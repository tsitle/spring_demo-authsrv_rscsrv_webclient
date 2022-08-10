package com.ts.springdemo.authserver.config

import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import com.ts.springdemo.authserver.mongoshim.MongoOAuth2AuthorizationService
import com.ts.springdemo.authserver.mongoshim.MongoRegisteredClientRepository
import com.ts.springdemo.authserver.repository.CustomOAuth2AuthorizationRepository
import com.ts.springdemo.authserver.service.oidc.CustomOidcUserInfoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*


@Configuration(proxyBeanMethods = false)
class AuthorizationServerConfig(
			@Autowired
			private val customOAuth2AuthorizationRepository: CustomOAuth2AuthorizationRepository,
			@Autowired
			private val customOidcUserInfoService: CustomOidcUserInfoService
		) {

	@Value("\${custom-app.auth-server.provider-issuer-url}")
	private val cfgProviderIssuerUrl: String = ""

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	fun authServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
		val authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer<HttpSecurity>()
		// map claims for JWT Authentication Token
		authorizationServerConfigurer.oidc { oidcConfigurer ->
				oidcConfigurer.userInfoEndpoint { userInfoConfigurer ->
						userInfoConfigurer.userInfoMapper { oidcUserInfoAuthenticationContext ->
								val authentication: OidcUserInfoAuthenticationToken = oidcUserInfoAuthenticationContext.getAuthentication()
								val principal = authentication.principal as JwtAuthenticationToken
								customOidcUserInfoService.getMappedPrincipalClaimsForUserInfoInJwtAuthToken(principal)
							}
					}
			}

		/* from OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http) : */
		val endpointsMatcher = authorizationServerConfigurer.endpointsMatcher

		http
				.requestMatcher(endpointsMatcher)
				.authorizeRequests { authorizeRequestsConfigurer ->
						authorizeRequestsConfigurer
								.anyRequest().authenticated()
					}
				.csrf { csrf: CsrfConfigurer<HttpSecurity?> ->
						csrf.ignoringRequestMatchers(
								endpointsMatcher
							)
					}
				.apply(authorizationServerConfigurer)
		/* end of OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http) */

		http
				.cors()
				.and()
				.csrf().disable()
				.oauth2ResourceServer { obj: OAuth2ResourceServerConfigurer<HttpSecurity?> -> obj.jwt() }  // this is required for the /userinfo endpoint
				.formLogin(Customizer.withDefaults())
		return http.build()
	}

	@Bean
	fun registeredClientRepository(mongoTemplate: MongoTemplate): RegisteredClientRepository {
		return MongoRegisteredClientRepository(mongoTemplate)
	}

	@Bean
	fun authorizationService(mongoTemplate: MongoTemplate, registeredClientRepository: RegisteredClientRepository): OAuth2AuthorizationService {
		return MongoOAuth2AuthorizationService(mongoTemplate, registeredClientRepository, customOAuth2AuthorizationRepository)
	}

	@Bean
	fun jwkSource(): JWKSource<SecurityContext?> {
		val rsaKey: RSAKey = generateRsa()
		val jwkSet = JWKSet(rsaKey)
		return JWKSource<SecurityContext?> { jwkSelector: JWKSelector, _: SecurityContext? ->
				jwkSelector.select(
						jwkSet
					)
			}
	}

	@Bean
	fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)
	}

	@Bean
	fun providerSettings(): ProviderSettings? {
		return ProviderSettings.builder()
				.issuer(cfgProviderIssuerUrl)
				.build()
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun generateRsa(): RSAKey {
		val keyPair: KeyPair = generateRsaKey()
		val publicKey: RSAPublicKey = keyPair.public as RSAPublicKey
		val privateKey: RSAPrivateKey = keyPair.private as RSAPrivateKey
		return RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build()
	}

	@Throws(IllegalStateException::class)
	private fun generateRsaKey(): KeyPair {
		val keyPair: KeyPair = try {
				val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
				keyPairGenerator.initialize(2048)
				keyPairGenerator.generateKeyPair()
			} catch (ex: Exception) {
				throw IllegalStateException(ex)
			}
		return keyPair
	}
}
