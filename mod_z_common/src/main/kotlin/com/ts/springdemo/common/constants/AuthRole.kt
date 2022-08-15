package com.ts.springdemo.common.constants

import org.springframework.security.core.authority.SimpleGrantedAuthority

@Suppress("unused")
class AuthRole {
	companion object {
		/** GrantedAuthority prefix */
		const val GA_PREFIX = "ROLE_"
		/** Key in OAuth2 Token claims for the authenticated user's role */
		const val CLAIM_AUTHUSER_ROLE_KEY = "authuser_role"

		fun buildAuthRole(role: EnRoles): String {
			return GA_PREFIX + role.value.uppercase()
		}

		@Throws(IllegalStateException::class)
		fun buildAuthRole(roleStr: String): String {
			val roleStrLower = roleStr.lowercase()
			for (roleEn in EnRoles.values()) {
				if (roleEn.value.lowercase() != roleStrLower) {
					continue
				}
				return GA_PREFIX + roleEn.value.uppercase()
			}
			throw IllegalStateException("invalid role '$roleStr'")
		}

		@Throws(IllegalStateException::class)
		fun buildAuthRoleFromArbitraryString(roleStr: String): String {
			if (roleStr.isEmpty()) {
				throw IllegalStateException("roleStr must not be empty")
			}
			return GA_PREFIX + roleStr.uppercase()
		}

		fun buildGrantedAuthority(role: EnRoles): SimpleGrantedAuthority {
			return SimpleGrantedAuthority(
					buildAuthRole(role)
				)
		}

		fun buildGrantedAuthority(roleStr: String): SimpleGrantedAuthority {
			return SimpleGrantedAuthority(
					buildAuthRole(roleStr)
				)
		}

		fun buildGrantedAuthorityFromArbitraryString(roleStr: String): SimpleGrantedAuthority {
			return SimpleGrantedAuthority(
					buildAuthRoleFromArbitraryString(roleStr)
				)
		}
	}

	enum class EnRoles(val value: String) {
		GRP_ADMIN("grpAdmin"),
		GRP_REGULAR("grpRegular"),
		GRP_NOBODY("grpNobody")
	}
}
