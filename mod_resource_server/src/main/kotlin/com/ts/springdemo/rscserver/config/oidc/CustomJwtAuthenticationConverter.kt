package com.ts.springdemo.rscserver.config.oidc

import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.common.constants.AuthRscAcc
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter


class CustomJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

	private val defaultGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

	override fun convert(source: Jwt): AbstractAuthenticationToken {
		val defaultAuthorities: Collection<GrantedAuthority>? = defaultGrantedAuthoritiesConverter.convert(source)
		val additionalAuthorities: Collection<GrantedAuthority> = extractAuthorities(source)
		val allAuthorities = mutableListOf<GrantedAuthority>()
		if (! defaultAuthorities.isNullOrEmpty()) {
			allAuthorities.addAll(defaultAuthorities)
		}
		allAuthorities.addAll(additionalAuthorities)
		return JwtAuthenticationToken(source, allAuthorities)
	}

	companion object {
		private val authoritiesMapper = SimpleAuthorityMapper()

		private fun getGrantedAuthRolesFromList(authList: List<String>): List<GrantedAuthority> {
			authoritiesMapper.setConvertToUpperCase(true)
			authoritiesMapper.setPrefix(AuthRole.GA_PREFIX)
			val authorities: Collection<GrantedAuthority> = AuthorityUtils
					.createAuthorityList(*authList.toTypedArray())
			return authoritiesMapper.mapAuthorities(authorities).toList()
		}

		private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
			val res = mutableListOf<GrantedAuthority>()
			//
			val authUserRole = jwt.getClaim<String>(AuthRole.CLAIM_AUTHUSER_ROLE_KEY)
			if (! authUserRole.isNullOrEmpty()) {
				val tmpStrList = mutableListOf<String>()
				try {
					tmpStrList.add(
							AuthRole.buildAuthRole(authUserRole)
						)
				} catch (err: IllegalStateException) {
					// silently ignore error
				}
				val tmpGaList = getGrantedAuthRolesFromList(tmpStrList)
				res.addAll(tmpGaList)
			}
			//
			val authUserRa = jwt.getClaim<String>(AuthRscAcc.CLAIM_AUTHUSER_RSCACC_ROLES_KEY)
			if (! authUserRa.isNullOrEmpty()) {
				val tmpStrList: List<String> = authUserRa.split(",")
				val tmpGaList = getGrantedAuthRolesFromList(tmpStrList)
				res.addAll(tmpGaList)
			}
			//
			return res
		}
	}
}
