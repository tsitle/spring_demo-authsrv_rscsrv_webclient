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
			private val cfgClientWebAppInternalClientId: String,
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
				.append("hasRole('${roReg}') or ")
				.append("hasRole('ROLE_USER')").toString()  // ROLE_USER only means that the AuthUser has the openid scope

		http
				.authorizeRequests { authorizeRequestsCustomizer ->
						authorizeRequestsCustomizer
								.antMatchers(HttpMethod.GET,
										"/logout/**", "/ui/logged_out/**",
										"/custom/authcode/fetch", "/custom/authcode/authorized"
									)
								.permitAll()
						authorizeRequestsCustomizer
								.antMatchers(HttpMethod.GET, pathUiRoot)
								.access(hasAuthForUiRoot)
						authorizeRequestsCustomizer
								.anyRequest().authenticated()
					}
				.oauth2Client(Customizer.withDefaults())
		if (cfgClientWebAppInternalClientId != "customauthcodetest-authorization_code") {
			http
					.oauth2Login { oauth2LoginCustomizer: OAuth2LoginConfigurer<HttpSecurity?> ->
							oauth2LoginCustomizer.loginPage(
									"/oauth2/authorization/$cfgClientWebAppInternalClientId"
								)
							oauth2LoginCustomizer.userInfoEndpoint().oidcUserService(customOidcUserService)
						}
		}
		return http.build()
	}
}
