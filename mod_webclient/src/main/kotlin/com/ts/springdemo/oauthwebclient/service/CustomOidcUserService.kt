package com.ts.springdemo.oauthwebclient.service

import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.common.constants.AuthRscAcc
import com.ts.springdemo.common.constants.AuthSrvOauth
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Service
import java.util.*


@Service
class CustomOidcUserService: OidcUserService() {

	companion object {
		private val OAUTH_ERR_INVALID_REQUEST: OAuth2Error = OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST)
	}

	private val authoritiesMapper: GrantedAuthoritiesMapper

	init {
		authoritiesMapper = SimpleAuthorityMapper()
		authoritiesMapper.setConvertToUpperCase(true)
	}

	/**
	 * Augments [OidcUserService.loadUser] to add authorities
	 * provided by Custom Authorization Server.
	 */
	override fun loadUser(userRequest: OidcUserRequest): OidcUser? {
		val user = super.loadUser(userRequest)
		val authorities: MutableSet<GrantedAuthority> = LinkedHashSet()
		authorities.addAll(user.authorities)
		authorities.addAll(
				extractAuthorities(userRequest)
			)
		return DefaultOidcUser(authorities, userRequest.idToken, user.userInfo, AuthSrvOauth.ATTR_USERINFO_USERNAME_KEY)
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Extracts [GrantedAuthority] from the AccessToken in the [OidcUserRequest].
	 */
	private fun extractAuthorities(userRequest: OidcUserRequest): Collection<GrantedAuthority> {
		val token: Jwt = parseJwt(userRequest.accessToken.tokenValue, userRequest.clientRegistration)

		val grantedRoles: MutableList<String> = mutableListOf()
		//
		val authUserRole = token.claims[AuthRole.CLAIM_AUTHUSER_ROLE_KEY] as String?
		if (! authUserRole.isNullOrEmpty()) {
			grantedRoles.add(
					AuthRole.buildAuthRole(authUserRole)
				)
		}
		//
		val authUserRscAccRoles = token.claims[AuthRscAcc.CLAIM_AUTHUSER_RSCACC_ROLES_KEY] as String?
		if (! authUserRscAccRoles.isNullOrEmpty()) {
			authUserRscAccRoles.split(",").forEach { itRole: String ->
					if (itRole.isNotEmpty()) {
						val tmpAuthRole: String = AuthRole.buildAuthRoleFromArbitraryString(itRole)
						if (! grantedRoles.contains(tmpAuthRole)) {
							grantedRoles.add(tmpAuthRole)
						}
					}
				}
		}
		//
		if (grantedRoles.isEmpty()) {
			return emptyList()
		}
		val authorities: Collection<GrantedAuthority> = AuthorityUtils
				.createAuthorityList(
						*grantedRoles.toTypedArray()
					)
		return authoritiesMapper.mapAuthorities(authorities)
	}

	@Throws(OAuth2AuthenticationException::class)
	private fun parseJwt(accessTokenValue: String, clientRegistration: ClientRegistration): Jwt {
		return try {
				val authSrvIssuerUrl = clientRegistration.providerDetails.issuerUri
				val authSrvJwksSetUri = clientRegistration.providerDetails.jwkSetUri
				val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(authSrvJwksSetUri).build()
				val jwtValidator = JwtValidators.createDefaultWithIssuer(authSrvIssuerUrl)
				jwtDecoder.setJwtValidator(jwtValidator)
				jwtDecoder.decode(accessTokenValue)
			} catch (err: JwtException) {
				throw OAuth2AuthenticationException(OAUTH_ERR_INVALID_REQUEST, err)
			}
	}
}
