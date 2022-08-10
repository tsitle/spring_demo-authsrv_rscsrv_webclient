package com.ts.springdemo.common.constants

import org.springframework.security.oauth2.core.AuthorizationGrantType


@Suppress("unused")
class OauthGrantType {
	enum class EnGrantType(val value: String) {
		AUTH_CODE(AuthorizationGrantType.AUTHORIZATION_CODE.value),
		CLIENT_CREDS(AuthorizationGrantType.CLIENT_CREDENTIALS.value),
		REFR_TOK(AuthorizationGrantType.REFRESH_TOKEN.value),
	}
}
