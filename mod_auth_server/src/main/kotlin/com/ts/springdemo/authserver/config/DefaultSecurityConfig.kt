package com.ts.springdemo.authserver.config

import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.authserver.service.CustomAuthenticationProvider
import com.ts.springdemo.common.constants.AuthRscAcc
import com.ts.springdemo.common.constants.AuthSrvOauth
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@EnableWebSecurity
class DefaultSecurityConfig(
			@Value("\${custom-app.client-web-app.url}")
			private val cfgClientWebAppUrl: String,
			@Value("\${custom-app.client-web-app.logged-out-page}")
			private val cfgClientWebAppLoggeOutPage: String,
			@Autowired
			private val customAuthenticationProvider: CustomAuthenticationProvider
		) {

	@Autowired
	fun bindAuthenticationProvider(authenticationManagerBuilder: AuthenticationManagerBuilder) {
		authenticationManagerBuilder
				.authenticationProvider(customAuthenticationProvider)
	}

	@Bean
	fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
		// URL paths
		val pathUserinfoInBrowser = "/ui/userinfo/in_browser"
		val pathUserinfoOauth = "/api/v1/userinfo/oauth"
		val pathUiRoot = "/"
		// roles
		val roAdmin = AuthRole.buildAuthRole(AuthRole.EnRoles.GRP_ADMIN)
		val roReg = AuthRole.buildAuthRole(AuthRole.EnRoles.GRP_REGULAR)
		val roUserinfoInBrowser = AuthRscAcc.buildAuthRole(
				AuthRscAcc.EnSrv.AUTH_SRV, "as_custom_userinfo_web", AuthRscAcc.EnMeth.GET
			)
		// StringBuilder() is only being used here to avoid IntelliJ's warning about a variable that cannot be resolved
		val hasAuthForUserinfoInBrowser: String = StringBuilder("hasRole('${roAdmin}') or ")
				.append("hasRole('${roUserinfoInBrowser}')").toString()
		val hasAuthForUiRoot: String = StringBuilder("hasRole('${roAdmin}') or ")
				.append("hasRole('${roReg}')").toString()

		http
				.csrf().disable()  // this is also done in AuthorizationServerConfig
				.authorizeRequests { authorizeRequestsCustomizer ->
						authorizeRequestsCustomizer
								.antMatchers(
										"/favicon.ico",
										"/login", "/login?logout", "/login?error",
										"/error/forbidden"
									)
								.permitAll()
						authorizeRequestsCustomizer
								.antMatchers(HttpMethod.GET, pathUserinfoInBrowser)
								.access(hasAuthForUserinfoInBrowser)
						authorizeRequestsCustomizer
								.antMatchers(HttpMethod.POST, pathUserinfoOauth)  // does it own authorization
								.permitAll()
						authorizeRequestsCustomizer
								.antMatchers(HttpMethod.GET, pathUiRoot)
								.access(hasAuthForUiRoot)
					}
				.formLogin(Customizer.withDefaults())  // this is also done in AuthorizationServerConfig
				.logout()
					.logoutSuccessHandler { request, response, _ ->
							var targetUrl = "/login"
							val param: String? = request.getParameter(AuthSrvOauth.URL_PARAM_LOGOUT_PAGE_REDIRECT_TO_WEBCLIENT)
							if (! param.isNullOrEmpty()) {
								targetUrl = "${cfgClientWebAppUrl}${cfgClientWebAppLoggeOutPage}"
							}
							response.sendRedirect(targetUrl)
						}
				.and()
				.exceptionHandling()
					.accessDeniedPage("/error/forbidden")
		return http.build()
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@Bean
	protected fun corsConfigurationSource(): CorsConfigurationSource {
		val source = UrlBasedCorsConfigurationSource()
		val corsConfig = CorsConfiguration().applyPermitDefaultValues().apply {
				allowedOrigins = listOf("localhost", "127.0.0.1")
				allowCredentials = true
				allowedHeaders = listOf("Origin", "Content-Type", "Accept", "responseType", "Authorization")
				allowedMethods = listOf("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH")
			}
		source.registerCorsConfiguration("/**", corsConfig)
		return source
	}
}
