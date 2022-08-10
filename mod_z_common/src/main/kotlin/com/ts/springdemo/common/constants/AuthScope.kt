package com.ts.springdemo.common.constants

import com.nimbusds.openid.connect.sdk.OIDCScopeValue


@Suppress("unused")
class AuthScope {
	companion object {
		/** GrantedAuthority prefix */
		@Suppress("WeakerAccess")
		const val GA_PREFIX = "SCOPE_"

		fun buildAuthScope(scope: EnScopes): String {
			return GA_PREFIX + scope.value
		}
	}

	enum class EnScopes(val value: String) {
		OPENID(OIDCScopeValue.OPENID.value),  // 'openid'
		OIDC_PROFILE(OIDCScopeValue.PROFILE.value),  // 'profile'
		OIDC_ADDRESS(OIDCScopeValue.ADDRESS.value),  // 'address'
		OIDC_EMAIL(OIDCScopeValue.EMAIL.value),  // 'email'
		OIDC_PHONE(OIDCScopeValue.PHONE.value),  // 'phone'
		OIDC_OFFLINE_ACCESS(OIDCScopeValue.OFFLINE_ACCESS.value),  // 'offline_access'
	}
}
