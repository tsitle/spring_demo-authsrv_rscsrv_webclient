package com.ts.springdemo.authserver.config.oidc

import com.ts.springdemo.authserver.entity.CustomAuthUser
import com.ts.springdemo.authserver.service.AccessTokenEnhancingService
import com.ts.springdemo.authserver.service.CustomAuthUserService
import com.ts.springdemo.authserver.service.oidc.CustomOidcUserInfoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2TokenType
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer


@Configuration
class TokenCustomizerConfig(
			@Autowired
			private val customAuthUserService: CustomAuthUserService
		) {

	@Bean
	fun tokenCustomizer(
				customOidcUserInfoService: CustomOidcUserInfoService,
				accessTokenEnhancingService: AccessTokenEnhancingService
			): OAuth2TokenCustomizer<JwtEncodingContext> {
		return OAuth2TokenCustomizer { context: JwtEncodingContext ->
				if (OidcParameterNames.ID_TOKEN == context.tokenType.value) {
					val userInfo: OidcUserInfo? = customOidcUserInfoService.loadUserForIdToken(
							context.getPrincipal<Authentication>().name,
							context.authorizedScopes
						)
					if (userInfo != null) {
						context.claims.claims { remClaims: MutableMap<String, Any> ->
								remClaims.putAll(userInfo.claims)
							}
					}
				} else if (OAuth2TokenType.ACCESS_TOKEN == context.tokenType) {
					val username: String? = context.getPrincipal<Authentication>().name
					val authUser: CustomAuthUser? = if (! username.isNullOrEmpty()) {
							customAuthUserService.findByUserEmail(username)
						} else {
							null
						}
					val clientScopes: List<String> = context.registeredClient.scopes.toList()
					//
					val addClaims: Map<String, Any> = accessTokenEnhancingService.getAdditionalClaims(authUser, clientScopes)
					context.claims.claims { remClaims: MutableMap<String, Any?> ->
							remClaims.putAll(addClaims)
						}
				}
		}
	}
}
