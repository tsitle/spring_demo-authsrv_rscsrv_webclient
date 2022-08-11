package com.ts.springdemo.authserver.mongoshim

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.ts.springdemo.authserver.entity.CustomOAuth2Authorization
import com.ts.springdemo.authserver.repository.CustomOAuth2AuthorizationRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.util.Assert
import org.springframework.web.server.ResponseStatusException
import java.util.function.Consumer


/**
 * Custom implementation of a OAuth2AuthorizationService for usage with MongoDB based on
 *   org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
 */
class MongoOAuth2AuthorizationService(
			private val mongoTemplate: MongoTemplate?,
			private val registeredClientRepository: RegisteredClientRepository,
			private val customOAuth2AuthorizationRepository: CustomOAuth2AuthorizationRepository?
		) : OAuth2AuthorizationService {

	private val objectMapper = ObjectMapper()

	init {
		val classLoader: ClassLoader? = JdbcOAuth2AuthorizationService::class.java.classLoader
		val securityModules: List<Module?>? = SecurityJackson2Modules.getModules(classLoader)
		this.objectMapper.registerModules(securityModules)
		this.objectMapper.registerModule(OAuth2AuthorizationServerJackson2Module())
	}

	override fun save(authorization: OAuth2Authorization) {
		Assert.notNull(authorization, "authorization cannot be null")
		val grantTypeStr: String = authorization.authorizationGrantType.value
		val tokenCont: OAuth2Authorization.Token<*>? = getTokenFromDefaultAuthorization(authorization)
		val existingCustom: CustomOAuth2Authorization? = this.findCustomByToken(
				tokenCont?.token?.tokenValue,
				OAuth2TokenType(grantTypeStr)
			)
		if (existingCustom != null) {
			mongoTemplate?.remove(existingCustom)
		}
		val custom = this.convertDefaultToCustomOAuth2Authorization(authorization) ?: return
		mongoTemplate?.save(custom)
	}

	override fun remove(authorization: OAuth2Authorization) {
		val custom = this.convertDefaultToCustomOAuth2Authorization(authorization) ?: return
		mongoTemplate?.remove(custom)
	}

	override fun findById(id: String): OAuth2Authorization? {
		Assert.hasText(id, "id cannot be empty")
		val custom: CustomOAuth2Authorization? = customOAuth2AuthorizationRepository?.findOneById(id)
		var res: OAuth2Authorization? = null
		if (custom != null) {
			res = convertCustomToDefaultOAuth2Authorization(custom)
		}
		return res
	}

	override fun findByToken(tokenVal: String?, tokenOrGrantType: OAuth2TokenType?): OAuth2Authorization? {
		val custom: CustomOAuth2Authorization = this.findCustomByToken(tokenVal, tokenOrGrantType) ?: return null
		return convertCustomToDefaultOAuth2Authorization(custom)
	}

	@Throws(ResponseStatusException::class)
	fun findByAuthorizationHeader(hdAuth: String?): OAuth2Authorization {
		if (hdAuth == null || hdAuth.isEmpty()) {
			throw ResponseStatusException(BAD_REQUEST, "Missing " + HttpHeaders.AUTHORIZATION)
		}
		if (! hdAuth.startsWith(OAuth2AccessToken.TokenType.BEARER.value + " ", ignoreCase=false)) {
			throw ResponseStatusException(BAD_REQUEST, "Invalid " + HttpHeaders.AUTHORIZATION)
		}
		var tokenVal: String = hdAuth.substring(OAuth2AccessToken.TokenType.BEARER.value.length + 1)
		tokenVal = tokenVal.trim()
		if (tokenVal.isEmpty()) {
			throw ResponseStatusException(BAD_REQUEST, "Invalid " + OAuth2AccessToken.TokenType.BEARER.value)
		}

		val oauth2Auth: OAuth2Authorization = findByToken(tokenVal, OAuth2TokenType.ACCESS_TOKEN)
				?: throw ResponseStatusException(BAD_REQUEST, "Invalid parameters")
		if (oauth2Auth.accessToken.isExpired || oauth2Auth.accessToken.isBeforeUse || oauth2Auth.accessToken.isInvalidated ||
				! oauth2Auth.accessToken.isActive) {
			throw ResponseStatusException(BAD_REQUEST, "Invalid parameters")
		}
		return oauth2Auth
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@Throws(IllegalArgumentException::class)
	private fun getTokenFromDefaultAuthorization(authorization: OAuth2Authorization): OAuth2Authorization.Token<*>? {
		val tokenCont: OAuth2Authorization.Token<*>? = when (authorization.authorizationGrantType) {
				AuthorizationGrantType.AUTHORIZATION_CODE -> authorization.getToken(OAuth2AuthorizationCode::class.java)
				AuthorizationGrantType.CLIENT_CREDENTIALS -> authorization.getToken(OAuth2AccessToken::class.java)
				AuthorizationGrantType.REFRESH_TOKEN -> authorization.getToken(OAuth2RefreshToken::class.java)
				else -> throw IllegalArgumentException("invalid AuthorizationGrantType")
			}
		return tokenCont
	}

	@Throws(IllegalArgumentException::class)
	private fun findCustomByToken(tokenVal: String?, tokenOrGrantType: OAuth2TokenType?): CustomOAuth2Authorization? {
		if (tokenVal == null || tokenVal.isEmpty() || tokenOrGrantType == null) {
			return null
		}
		val ttKey: String = when (tokenOrGrantType.value) {
				AuthorizationGrantType.AUTHORIZATION_CODE.value, OAuth2ParameterNames.CODE -> "tokenAuthCodeVal"
				AuthorizationGrantType.CLIENT_CREDENTIALS.value, OAuth2TokenType.ACCESS_TOKEN.value -> "tokenAccTokVal"
				AuthorizationGrantType.REFRESH_TOKEN.value -> "tokenRefrTokVal"
				else -> throw IllegalArgumentException("invalid tokenOrGrantType")
			}
		val customArr: List<CustomOAuth2Authorization?>? = customOAuth2AuthorizationRepository?.findByTokenTypeAndTokenValue(ttKey, tokenVal)
		if (customArr != null && customArr.size == 1) {
			return customArr[0]
		}
		return null
	}

	private fun convertDefaultToCustomOAuth2Authorization(authorization: OAuth2Authorization): CustomOAuth2Authorization? {
		val regClient: RegisteredClient = registeredClientRepository.findById(authorization.registeredClientId)
				?: return null
		val agtObj = authorization.authorizationGrantType
		val tempEntry = CustomOAuth2Authorization.withRegisteredClient(regClient)
				.id(authorization.id)
				.principalName(authorization.principalName)
				.authorizationGrantTypeStr(agtObj.value)
				.state(authorization.attributes[OAuth2ParameterNames.STATE] as String?)
		tempEntry.attributesStr(
				this.convertObjMapToStr(authorization.attributes)
			)
		authorization.getToken(OAuth2AuthorizationCode::class.java)?.let { elemTok ->
				tempEntry
						.tokenAuthCodeVal(elemTok.token.tokenValue)
						.tokenAuthCodeIa(elemTok.token.issuedAt)
						.tokenAuthCodeEa(elemTok.token.expiresAt)
						.tokenAuthCodeMetaStr(this.convertObjMapToStr(elemTok.metadata))
			}
		authorization.accessToken?.let { elemTok ->
				tempEntry
						.tokenAccTokVal(elemTok.token.tokenValue)
						.tokenAccTokIa(elemTok.token.issuedAt)
						.tokenAccTokEa(elemTok.token.expiresAt)
						.tokenAccTokMetaStr(this.convertObjMapToStr(elemTok.metadata))
			}
		authorization.refreshToken?.let { elemTok ->
				tempEntry
						.tokenRefrTokVal(elemTok.token.tokenValue)
						.tokenRefrTokIa(elemTok.token.issuedAt)
						.tokenRefrTokEa(elemTok.token.expiresAt)
						.tokenRefrTokMetaStr(this.convertObjMapToStr(elemTok.metadata))
			}
		authorization.getToken(OidcIdToken::class.java)?.let { elemTok ->
				tempEntry
						.tokenOidcTokVal(elemTok.token.tokenValue)
						.tokenOidcTokIa(elemTok.token.issuedAt)
						.tokenOidcTokEa(elemTok.token.expiresAt)
						.tokenOidcTokMetaStr(this.convertObjMapToStr(elemTok.metadata))
			}
		return tempEntry.build()
	}

	private fun convertCustomToDefaultOAuth2Authorization(custom: CustomOAuth2Authorization): OAuth2Authorization? {
		val regClient: RegisteredClient = registeredClientRepository.findById(custom.getRegisteredClientId())
				?: return null
		val agtObj = AuthorizationGrantType(custom.getAuthorizationGrantTypeStr())
		val tempEntry = OAuth2Authorization.withRegisteredClient(regClient)
				.id(custom.getId())
				.principalName(custom.getPrincipalName())
				.authorizationGrantType(agtObj)
		val attrStr: String? = custom.getAttributesStr()
		if (attrStr != null) {
			tempEntry.attributes(
					this.convertStrToObjMapConsumer(attrStr)
				)
		}
		val stateStr: String? = custom.getState()
		if (stateStr != null) {
			tempEntry.attribute(OAuth2ParameterNames.STATE, stateStr)
		}
		//
		var tokenValStr: String? = custom.getTokenAuthCodeVal()
		var metaStr: String? = custom.getTokenAuthCodeMetaStr()
		if (tokenValStr != null && metaStr != null) {
			val tokenObj = OAuth2AuthorizationCode(
					tokenValStr,
					custom.getTokenAuthCodeIa(),
					custom.getTokenAuthCodeEa()
				)
			tempEntry.token(
					tokenObj,
					this.convertStrToObjMapConsumer(metaStr)
				)
		}
		//
		tokenValStr = custom.getTokenAccTokVal()
		metaStr = custom.getTokenAccTokMetaStr()
		if (tokenValStr != null && metaStr != null) {
			val metaMap = convertStrToObjMap(metaStr)
			@Suppress("UNCHECKED_CAST")
			val scopes: Set<String> = (metaMap["metadata.token.claims"] as Map<String, Any>?)?.get("scope") as Set<String>?
					?: emptySet()
			val tokenObj = OAuth2AccessToken(
					OAuth2AccessToken.TokenType.BEARER,
					tokenValStr,
					custom.getTokenAccTokIa(),
					custom.getTokenAccTokEa(),
					scopes
				)
			tempEntry.token(
					tokenObj,
					this.convertStrToObjMapConsumer(metaStr)
				)
		}
		//
		tokenValStr = custom.getTokenRefrTokVal()
		metaStr = custom.getTokenRefrTokMetaStr()
		if (tokenValStr != null && metaStr != null) {
			val tokenObj = OAuth2RefreshToken(
					tokenValStr,
					custom.getTokenRefrTokIa(),
					custom.getTokenRefrTokEa()
				)
			tempEntry.token(
					tokenObj,
					this.convertStrToObjMapConsumer(metaStr)
				)
		}
		//
		tokenValStr = custom.getTokenOidcTokVal()
		metaStr = custom.getTokenOidcTokMetaStr()
		if (tokenValStr != null && metaStr != null) {
			val tokenMeta = this.convertStrToObjMap(metaStr)
			@Suppress("UNCHECKED_CAST")
			val tokenMetaClaims: Map<String, Any?>? = tokenMeta[OAuth2Authorization.Token.CLAIMS_METADATA_NAME] as Map<String, Any?>?
			if (tokenMetaClaims != null) {
				val tokenObj = OidcIdToken(
						tokenValStr,
						custom.getTokenOidcTokIa(),
						custom.getTokenOidcTokEa(),
						tokenMetaClaims
					)
				tempEntry.token(
						tokenObj,
						this.convertStrToObjMapConsumer(metaStr)
					)
			}
		}
		return tempEntry.build()
	}

	@Throws(IllegalArgumentException::class)
	private fun convertObjMapToStr(inpMap: Map<String, Any>): String {
		try {
			return objectMapper.writeValueAsString(inpMap)
		} catch (ex: Exception) {
			throw IllegalArgumentException(ex.message, ex)
		}
	}

	@Throws(IllegalArgumentException::class)
	private fun convertStrToObjMap(inpMapStr: String): Map<String, Any?> {
		try {
			return objectMapper.readValue(inpMapStr, object : TypeReference<Map<String, Any?>>() {})
		} catch (ex: java.lang.Exception) {
			throw IllegalArgumentException(ex.message, ex)
		}
	}

	private fun convertStrToObjMapConsumer(inpMapStr: String): Consumer<MutableMap<String, Any>> {
		return Consumer { outpMap: MutableMap<String, Any> ->
				val tempMap: Map<String, Any?> = convertStrToObjMap(inpMapStr)
				tempMap.iterator().forEach { itEntry ->
						if (itEntry.value != null) {
							outpMap[itEntry.key] = itEntry.value!!
						}
					}
			}
	}
}
