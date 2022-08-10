package com.ts.springdemo.oauthwebclient.config

import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.oauthwebclient.service.CustomOidcUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SecurityConfig(
			@Value("\${custom-app.client-web-app.internal-client-id}")
			private val cfgClientWebAppRegClientId: String,
			@Autowired
			private val customOidcUserService: CustomOidcUserService
		) {

	@Bean
	@Throws(Exception::class)
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		// URL paths
		val pathUiRoot = "/"
		// roles
		val roAdmin = AuthRole.buildAuthRole(AuthRole.EnRoles.GRP_ADMIN)
		val roReg = AuthRole.buildAuthRole(AuthRole.EnRoles.GRP_REGULAR)
		// StringBuilder() is only being used here to avoid IntelliJ's warning about a variable that cannot be resolved
		val hasAuthForUiRoot: String = StringBuilder("hasRole('${roAdmin}') or ")
				.append("hasRole('${roReg}')").toString()

		http
				.authorizeRequests { authorizeRequestsCustomizer ->
						authorizeRequestsCustomizer
								.antMatchers(HttpMethod.GET, "/logout/**", "/logged_out/**", "/error/forbidden/**")
								.permitAll()
						authorizeRequestsCustomizer
								.antMatchers(HttpMethod.GET, pathUiRoot)
								.access(hasAuthForUiRoot)
						authorizeRequestsCustomizer
								.anyRequest().authenticated()
					}
				.oauth2Login { oauth2LoginCustomizer: OAuth2LoginConfigurer<HttpSecurity?> ->
						oauth2LoginCustomizer.loginPage(
								"/oauth2/authorization/$cfgClientWebAppRegClientId"
							)
						oauth2LoginCustomizer.userInfoEndpoint().oidcUserService(customOidcUserService)
					}
				.oauth2Client(Customizer.withDefaults())
				.exceptionHandling()
						.accessDeniedPage("/error/forbidden")
		return http.build()
	}
}
