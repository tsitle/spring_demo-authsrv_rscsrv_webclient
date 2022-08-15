package com.ts.springdemo.authserver.ctrlapi

import com.ts.springdemo.authserver.mongoshim.MongoOAuth2AuthorizationService
import com.ts.springdemo.authserver.service.CustomAuthUserService
import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.common.constants.AuthRscAcc
import com.ts.springdemo.common.constants.AuthScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
class ApiCustomUserinfoController(
			@Autowired
			private val customAuthUserService: CustomAuthUserService,
			@Autowired
			private val authorizationService: OAuth2AuthorizationService
		) {

	/** This endpoint can be reached with a Basic Authentication */
	@PostMapping("/api/v1/userinfo/basicauth")
	fun postUserinfoBasicAuth(authentication: UsernamePasswordAuthenticationToken): Map<String, Any> {
		if (authentication.principal == null) {
			throw ResponseStatusException(BAD_REQUEST, "Missing Authentication")
		}
		return getUserInfo(authentication.principal as String?, customAuthUserService)
	}

	/**
	 * This endpoint can be reached with an OAuth2 Authorization Code or Client Credentials grant.
	 * It validates the HTTP Authorization Header itself and determines whether the client
	 * is allowed to access this endpoint.
	 */
	@PostMapping("/api/v1/userinfo/oauth")
	fun postUserinfoOauth(@RequestHeader(HttpHeaders.AUTHORIZATION) hdAuth: String?, queryUserEmail: String?): Map<String, Any> {
		val oauth2Auth: OAuth2Authorization = (authorizationService as MongoOAuth2AuthorizationService).findByAuthorizationHeader(hdAuth)
		// make sure the token has the required authorities
		authorizeToken(
				oauth2Auth,
				AuthScope.EnScopes.OPENID.value,
				orRole=false
			)
		authorizeToken(
				oauth2Auth,
				AuthRscAcc.buildRule(
						AuthRscAcc.EnSrv.AUTH_SRV, "as_custom_userinfo_oauth_api", AuthRscAcc.EnMeth.POST
					),
				orRole=true
			)

		// now we can render the result
		return getUserInfo(queryUserEmail, customAuthUserService)
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@Throws(ResponseStatusException::class)
	private fun authorizeToken(oauth2Auth: OAuth2Authorization, requiredScope: String, orRole: Boolean) {
		@Suppress("UNCHECKED_CAST")
		val authorizedScopes: List<String> = (oauth2Auth.accessToken.claims?.get("scope") as Set<String>?)?.toList()
				?: emptyList()
		var hasRole = false
		if (orRole) {
			val authorizedRoles: List<GrantedAuthority> =
					(oauth2Auth.attributes["java.security.Principal"] as UsernamePasswordAuthenticationToken?)?.authorities?.toList()
					?: emptyList()
			val requiredRoleAdmin: GrantedAuthority = AuthRole.buildGrantedAuthority(AuthRole.EnRoles.GRP_ADMIN)
			val requiredRoleRsc: GrantedAuthority = AuthRole.buildGrantedAuthorityFromArbitraryString(requiredScope)

			hasRole = authorizedRoles.contains(requiredRoleAdmin) || authorizedRoles.contains(requiredRoleRsc)
		}
		if (! (authorizedScopes.contains(requiredScope) || hasRole)) {
			throw ResponseStatusException(FORBIDDEN, "Missing scope/role '${requiredScope}'")
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	companion object {
		@Throws(ResponseStatusException::class)
		fun getUserInfo(userEmail: String?, customAuthUserService: CustomAuthUserService): Map<String, Any> {
			if (userEmail.isNullOrEmpty()) {
				throw ResponseStatusException(BAD_REQUEST, "Missing Username")
			}
			val userDetails = customAuthUserService.loadUserByUsername(userEmail)
			//
			val res = HashMap<String, Any>()
			res["principal"] = userDetails.username
			val authoritiesList = mutableListOf<String>()
			userDetails.authorities.forEach {
					authoritiesList.add(it.authority.toString())
				}
			res["authorities"] = authoritiesList
			return res
		}
	}
}
