package com.ts.springdemo.common.constants

@Suppress("unused")
class AuthSrvOauth {
	companion object {
		/** Path within URI for the authorization endpoint */
		const val URL_PATH_AUTHORIZATION = "/oauth2/authorize"
		/** Path within URI for the token endpoint */
		const val URL_PATH_TOKEN = "/oauth2/token"
		/** Path within URI for the JSON Web Key (JWK) Set endpoint */
		const val URL_PATH_JWK_SET = "/oauth2/jwks"
		/** Path within URI for the user info endpoint */
		const val URL_PATH_USERINFO = "/userinfo"

		/** Attribute name used to access the user's name from the OpenID user info response */
		const val ATTR_USERINFO_USERNAME_KEY = "sub"

		/** URL Parameter that tells the Auth Server to redirect the browser to the Web Client's 'You Have Been Logged Out' page */
		const val URL_PARAM_LOGOUT_PAGE_REDIRECT_TO_WEBCLIENT = "redirect_to_logged_out"
	}
}
