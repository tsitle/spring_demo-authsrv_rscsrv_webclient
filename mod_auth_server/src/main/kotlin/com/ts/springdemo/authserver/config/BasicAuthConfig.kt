package com.ts.springdemo.authserver.config

import com.ts.springdemo.authserver.config.misc.CustomBasicAuthEntryPoint
import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.common.constants.AuthRscAcc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain


@Configuration
class BasicAuthConfig(
			@Autowired
			private val customAuthenticationEntryPoint: CustomBasicAuthEntryPoint
		) {

	@Bean
	fun basicAuthSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
		// URL paths
		val pathUserinfoBasicauth = "/api/v1/userinfo/basicauth"
		// roles
		val roAdmin = AuthRole.buildAuthRole(AuthRole.EnRoles.GRP_ADMIN)
		val roUserinfoBasicauth = AuthRscAcc.buildAuthRole(
				AuthRscAcc.EnSrv.AUTH_SRV, "as_custom_userinfo_basicauth_api", AuthRscAcc.EnMeth.GET
			)
		// StringBuilder() is only being used here to avoid IntelliJ's warning about a variable that cannot be resolved
		val hasAuthForUserinfoBasic: String = StringBuilder("hasRole('${roAdmin}') or ")
				.append("hasRole('${roUserinfoBasicauth}')").toString()

		http
				.csrf().disable()  // this is also done in AuthorizationServerConfig
				.authorizeRequests { authorizeRequestsCustomizer ->
					authorizeRequestsCustomizer
							.antMatchers(HttpMethod.POST, pathUserinfoBasicauth)
							.access(hasAuthForUserinfoBasic)
					authorizeRequestsCustomizer.anyRequest().authenticated()
				}
				.antMatcher(pathUserinfoBasicauth)
					.httpBasic().authenticationEntryPoint(customAuthenticationEntryPoint)
				.and()
				.formLogin(Customizer.withDefaults())  // this is also done in AuthorizationServerConfig
		return http.build()
	}
}
