package com.ts.springdemo.authserver

import com.ts.springdemo.common.constants.AuthRscAcc
import com.ts.springdemo.common.constants.AuthScope
import com.ts.springdemo.common.constants.OauthGrantType
import com.ts.springdemo.authserver.entity.CustomAuthUser
import com.ts.springdemo.authserver.entity.RscIdPaths
import com.ts.springdemo.authserver.entity.oidc.CustomOidcUserInfo
import com.ts.springdemo.authserver.mongoshim.MongoRegisteredClientRepository
import com.ts.springdemo.authserver.repository.CustomAuthUserRepository
import com.ts.springdemo.authserver.repository.CustomOAuth2AuthorizationRepository
import com.ts.springdemo.authserver.repository.RscIdPathsRepository
import com.ts.springdemo.authserver.repository.oidc.CustomOidcUserInfoRepository
import com.ts.springdemo.authserver.service.RscIdPathsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.TokenSettings
import java.time.Duration
import java.time.Instant
import java.util.*


@SpringBootApplication
@EnableConfigurationProperties(CustomAppProperties::class)
class SpringDemoAuthServerApplication(
			@Autowired
			private val customAppProperties: CustomAppProperties,
			@Autowired
			private val passwordEncoder: PasswordEncoder,
			@Autowired
			private val customAuthUserRepository: CustomAuthUserRepository,
			@Autowired
			private val registeredClientRepository: RegisteredClientRepository,
			@Autowired
			private val customOidcUserInfoRepository: CustomOidcUserInfoRepository,
			@Autowired
			private val rscIdPathsRepository: RscIdPathsRepository,
			@Autowired
			private val rscIdPathsService: RscIdPathsService,
			@Autowired
			private val mongoTemplate: MongoTemplate,
			@Autowired
			private val customOAuth2AuthorizationRepository: CustomOAuth2AuthorizationRepository
) {

	@Bean
	fun init(): CommandLineRunner? {
		return CommandLineRunner {
				// bogus operation for initializing the DB connection
				customAuthUserRepository.findByEmail("does.not@exist")
				//
				println("----------------------------------------")
				println("----------------------------------------")
				println("----------------------------------------")
				println("----------------------------------------")
				if (customAppProperties.authServer.db.pruneCollections) {
					pruneCollections()
					println("----------------------------------------")
				}
				if (customAppProperties.authServer.db.initCollections) {
					initResourceIdToPathsMappings()
					println("----------------------------------------")
					initCustomAuthUsers()
					println("----------------------------------------")
					initCustomOidcUserInfos()
					println("----------------------------------------")
					initRegisteredClientRepository()
					println("----------------------------------------")
				}
				val cwaUrl = customAppProperties.clientWebApp.url
				println("ClientWebApp URL='$cwaUrl' (important for Authentication Code)")
				val piUrl = customAppProperties.authServer.providerIssuerUrl
				println("Provider Issuer URL='$piUrl' (important for JWT)")
				println("----------------------------------------")
				println("----------------------------------------")
				println("----------------------------------------")
				println("----------------------------------------")
			}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun pruneCollections() {
		println("Prune Collections")
		println("  - rscIdPaths")
		rscIdPathsRepository.deleteAll()
		println("  - authUser")
		customAuthUserRepository.deleteAll()
		println("  - oidcUserInfo")
		customOidcUserInfoRepository.deleteAll()
		println("  - customOauth2Authorization")
		customOAuth2AuthorizationRepository.deleteAll()
		//
		println("  - registeredClient")
		val mongoRegClientRepo = MongoRegisteredClientRepository(mongoTemplate)
		mongoRegClientRepo.deleteAll()
	}

	private fun initResourceIdToPathsMappings() {
		customAppProperties.resourceIdToUrlPaths.forEach { (itRscId: String, itRscEntry: CustomAppProperties.ResourceIdToUrlPathsEntry) ->
				val dbExistingMap: Optional<RscIdPaths?> = rscIdPathsRepository.findById(itRscId)
				//
				println("ResourceIdToPathsMapping: RscID='$itRscId'")
				//
				if (dbExistingMap.isPresent) {
					println("  --> already existed. skipping.")
				} else {
					val dbRscIdPaths = RscIdPaths.withId(itRscId)
							.srv(itRscEntry.srv)
							.enabled(true)
							.paths(itRscEntry.paths)
							.build()
					rscIdPathsRepository.save(dbRscIdPaths)
				}
			}
	}

	private fun initCustomAuthUsers() {
		customAppProperties.authServer.users.forEach {
				val userEmail = it.value.email
				val userRole = it.value.role
				val dbExistingUser: CustomAuthUser? = customAuthUserRepository.findByEmail(userEmail)
				//
				println("AuthUser: Email='$userEmail', Role='" + userRole.value + "'")
				//
				if (dbExistingUser != null) {
					println("  --> already existed. skipping.")
				} else {
					val convRscAcc = convertRscAccMap(it.value.rolesResourceAccess)
					val dbNewUser = CustomAuthUser.withRandomId()
							.email(userEmail)
							.password(passwordEncoder.encode(it.value.password))
							.enabled(true)
							.role(userRole)
							.resourceAccess(convRscAcc)
							.build()
					customAuthUserRepository.save(dbNewUser)
				}
			}
	}

	@Throws(IllegalStateException::class)
	private fun initCustomOidcUserInfos() {
		customAppProperties.authServer.users.forEach {
				val userEmail = it.value.email
				val dbAuthUser: CustomAuthUser = customAuthUserRepository.findByEmail(userEmail) ?:
						throw IllegalStateException("missing AuthUser '$userEmail'")
				val dbExistingUserInfo: CustomOidcUserInfo? = customOidcUserInfoRepository.findByAuthUserId(dbAuthUser.getId())
				//
				println("OidcUserInfo for AuthUser: Email='$userEmail'")
				//
				if (dbExistingUserInfo != null) {
					println("  --> already existed. skipping.")
				} else {
					val dbUserInfoBuilder = CustomOidcUserInfo.withRandomId()
					dbUserInfoBuilder.authUserId(dbAuthUser.getId())
					dbUserInfoBuilder.updatedAt(Instant.now())
					it.value.oidcInfo?.let { itOidc ->
							dbUserInfoBuilder.familyName(itOidc.familyName)
							dbUserInfoBuilder.givenName(itOidc.givenName)
							dbUserInfoBuilder.middleName(itOidc.middleName)
							dbUserInfoBuilder.nickName(itOidc.nickName)
							dbUserInfoBuilder.profileUrl(itOidc.profileUrl)
							dbUserInfoBuilder.pictureUrl(itOidc.pictureUrl)
							dbUserInfoBuilder.websiteUrl(itOidc.websiteUrl)
							dbUserInfoBuilder.emailVerified(itOidc.emailVerified)
							dbUserInfoBuilder.gender(itOidc.gender)
							dbUserInfoBuilder.birthdate(itOidc.getBirthdateAsDate())
							dbUserInfoBuilder.timezone(itOidc.timezone)
							dbUserInfoBuilder.locale(itOidc.locale)
							dbUserInfoBuilder.phoneNumber(itOidc.phoneNumber)
							dbUserInfoBuilder.phoneNumberVerified(itOidc.phoneNumberVerified)
							if (itOidc.address != null) {
								dbUserInfoBuilder.addressStreetAddress(itOidc.address!!.streetAddress)
								dbUserInfoBuilder.addressPostalCode(itOidc.address!!.postalCode)
								dbUserInfoBuilder.addressLocality(itOidc.address!!.locality)
								dbUserInfoBuilder.addressRegion(itOidc.address!!.region)
								dbUserInfoBuilder.addressCountry(itOidc.address!!.country)
							}
						}
					customOidcUserInfoRepository.save(
							dbUserInfoBuilder.build()
						)
				}
			}
	}

	private fun initRegisteredClientRepository() {
		customAppProperties.authServer.oauth2Clients.forEach {
				addRegisteredClient(it.key, it.value)
			}
	}

	private fun addRegisteredClient(
				clientId: String,
				clientConf: CustomAppProperties.AuthServer.OAuth2ClientConf
			) {
		println("OAuth2 clientID='$clientId', AuthorizationGrantTypes=" + clientConf.authorizationGrantTypes.toString())
		//
		val dbExistingClient = registeredClientRepository.findByClientId(clientId)
		if (dbExistingClient != null) {
			println("  --> already existed. skipping.")
			return
		}
		val clientSecret = clientConf.clientSecret
		val scopesList: List<String> = getScopesList(clientConf.additionalScopes, clientConf.scopesResourceAccess)
		val cwaUrl = customAppProperties.clientWebApp.url

		val dbRegClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.tokenSettings(
						TokenSettings.builder()
								.accessTokenTimeToLive(Duration.ofMinutes(5))
								.refreshTokenTimeToLive(Duration.ofMinutes(60))
								.reuseRefreshTokens(true)  // reuseRefreshTokens() doesn't seem to have any effect
								.build()
					)
				.clientId(clientId)
				.clientSecret(passwordEncoder.encode(clientSecret))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.scopes { remScopesList ->
						remScopesList.addAll(scopesList)
					}

		val clientAgts: List<OauthGrantType.EnGrantType> = clientConf.authorizationGrantTypes
		if (clientAgts.contains(OauthGrantType.EnGrantType.AUTH_CODE)) {
			val cwaCPT: List<String> = customAppProperties.clientWebApp.oauthCallbackPagesTemplates
			val asEPC = customAppProperties.authServer.enablePostmanCallback
			dbRegClient
					.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
					.redirectUris { remRedirectList ->
							if (asEPC) {
								remRedirectList.add("https://oauth.pstmn.io/v1/callback")
							}
							cwaCPT.forEach {
									val tempStr = it
											.trim()
											.replace("{clientId}", clientId)
									if (tempStr.isNotEmpty()) {
										remRedirectList.add(cwaUrl + tempStr)
									}
								}
						}
		}
		if (clientAgts.contains(OauthGrantType.EnGrantType.REFR_TOK)) {
			dbRegClient.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
		}
		if (clientAgts.contains(OauthGrantType.EnGrantType.CLIENT_CREDS)) {
			dbRegClient.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
		}

		registeredClientRepository.save(dbRegClient.build())
	}

	private fun getScopesList(
				addScopes: List<AuthScope.EnScopes>,
				resourceAccess: Map<String, CustomAppProperties.AuthServer.ResourceAccessUriMethods>?
			) : List<String> {
		val scopesList = mutableListOf<String>()
		scopesList.add(AuthScope.EnScopes.OPENID.value)
		if (addScopes.isNotEmpty()) {
			addScopes.forEach {
					if (! scopesList.contains(it.value)) {
						scopesList.add(it.value)
					}
				}
		}
		val convRscAcc = convertRscAccMap(resourceAccess)
		if (convRscAcc.isNotEmpty()) {
			val rscIdSrvMap = rscIdPathsService.getRscIdToSrvMap(convRscAcc.keys.toList())
			val rscAccScopesList = AuthRscAcc.convertMapToRulesList(convRscAcc, rscIdSrvMap)
			rscAccScopesList.forEach {
					if (! scopesList.contains(it)) {
						scopesList.add(it)
					}
				}
		}
		return scopesList
	}

	private fun convertRscAccMap(
				resourceAccess: Map<String, CustomAppProperties.AuthServer.ResourceAccessUriMethods>?
			): MutableMap<String, List<AuthRscAcc.EnMeth>> {
		val res: MutableMap<String, List<AuthRscAcc.EnMeth>> = HashMap()
		if (! resourceAccess.isNullOrEmpty()) {
			resourceAccess.forEach { (uriId, meths) ->
					res[uriId] = meths.methods
				}
		}
		return res
	}
}

// -----------------------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------

fun main(args: Array<String>) {
	runApplication<SpringDemoAuthServerApplication>(*args)
}
