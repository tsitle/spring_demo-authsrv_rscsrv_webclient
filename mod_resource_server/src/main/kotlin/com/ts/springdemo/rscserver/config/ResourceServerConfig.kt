package com.ts.springdemo.rscserver.config

import com.ts.springdemo.rscserver.config.oidc.CustomJwtAuthenticationConverter
import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.common.constants.AuthRscAcc
import com.ts.springdemo.common.constants.AuthScope
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain


@EnableWebSecurity
class ResourceServerConfig {
	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		// scopes
		val scOpenId = AuthScope.buildAuthScope(AuthScope.EnScopes.OPENID)
		// roles
		val roAdmin = AuthRole.buildAuthRole(AuthRole.EnRoles.GRP_ADMIN)
		val roRscAccArticlesGet = AuthRscAcc.buildAuthRole(
				AuthRscAcc.EnSrv.RSC_SRV, "rs_articles_api", AuthRscAcc.EnMeth.GET
			)
		val roRscAccProductsGet = AuthRscAcc.buildAuthRole(
				AuthRscAcc.EnSrv.RSC_SRV, "rs_products_api", AuthRscAcc.EnMeth.GET
			)
		val roRscAccUserinfoPost = AuthRscAcc.buildAuthRole(
				AuthRscAcc.EnSrv.RSC_SRV, "rs_custom_userinfo_api", AuthRscAcc.EnMeth.POST
			)
		// StringBuilder() is only being used here to avoid IntelliJ's warning about a variable that cannot be resolved
		val hasAuthForArt: String = StringBuilder("hasRole('${roRscAccArticlesGet}')").toString()
		val hasAuthForProd: String = StringBuilder("hasRole('${roRscAccProductsGet}')").toString()
		val hasAuthForUserinfo: String = StringBuilder("hasRole('${roAdmin}') or (")
					.append("hasAuthority('${scOpenId}') and ")
					.append("hasRole('${roRscAccUserinfoPost}')")
				.append(")").toString()

		http
				.authorizeRequests()
					.antMatchers(HttpMethod.GET, "/api/v1/articles/**").access(hasAuthForArt)
					.antMatchers(HttpMethod.GET, "/api/v1/products/**").access(hasAuthForProd)
					.antMatchers(HttpMethod.POST, "/api/v1/userinfo/**").access(hasAuthForUserinfo)
					.anyRequest().authenticated()
				.and()
				.oauth2ResourceServer()
				.jwt()
				.jwtAuthenticationConverter(
						CustomJwtAuthenticationConverter()
					)
		return http.build()
	}
}
