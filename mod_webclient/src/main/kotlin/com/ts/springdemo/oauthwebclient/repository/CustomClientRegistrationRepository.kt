package com.ts.springdemo.oauthwebclient.repository

import com.ts.springdemo.oauthwebclient.CustomAppProperties
import com.ts.springdemo.oauthwebclient.CustomAppProperties.ClientWebApp.OAuth2ClientConf
import com.ts.springdemo.common.constants.AuthScope
import com.ts.springdemo.common.constants.AuthSrvOauth
import com.ts.springdemo.common.constants.OauthGrantType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.stereotype.Repository


@Repository
class CustomClientRegistrationRepository(
			@Autowired
			private val customAppProperties: CustomAppProperties
		) : ClientRegistrationRepository {

	private val cfgAuthSrvIssuerUrl: String
	private val cfgClientWebAppUrl: String
	private val cfgOauth2Clients: Map<String, OAuth2ClientConf>

	init {
		customAppProperties.let {
			cfgAuthSrvIssuerUrl = it.authServer.providerIssuerUrl
			cfgClientWebAppUrl = it.clientWebApp.url
			cfgOauth2Clients = it.clientWebApp.oauth2Clients
		}
	}

	override fun findByRegistrationId(registrationId: String?): ClientRegistration? {
		if (registrationId!!.isEmpty()) {
			return null
		}
		val client: OAuth2ClientConf = cfgOauth2Clients[registrationId]
				?: throw IllegalStateException("invalid internalClientId '$registrationId'")

		if (client.authorizationGrantType == OauthGrantType.EnGrantType.AUTH_CODE) {
			return buildClientRegistrationAuthCode(registrationId, client)
		}
		if (client.authorizationGrantType == OauthGrantType.EnGrantType.CLIENT_CREDS) {
			return buildClientRegistrationClientCreds(registrationId, client)
		}
		return null
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun buildClientRegistrationAuthCode(internalClientId: String, client: OAuth2ClientConf): ClientRegistration {
		val cr = ClientRegistration.withRegistrationId(internalClientId)
				.clientId(client.clientId)
				.clientSecret(client.clientSecret)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationUri(cfgAuthSrvIssuerUrl + AuthSrvOauth.URL_PATH_AUTHORIZATION)
				.tokenUri(cfgAuthSrvIssuerUrl + AuthSrvOauth.URL_PATH_TOKEN)
				.jwkSetUri(cfgAuthSrvIssuerUrl + AuthSrvOauth.URL_PATH_JWK_SET)
				.issuerUri(cfgAuthSrvIssuerUrl)
				.userInfoUri(cfgAuthSrvIssuerUrl + AuthSrvOauth.URL_PATH_USERINFO)
				.userNameAttributeName(AuthSrvOauth.ATTR_USERINFO_USERNAME_KEY)
		cr.redirectUri(
				"$cfgClientWebAppUrl/login/oauth2/code/$internalClientId"
				/*"$cfgClientWebAppUrl/authorized"*/
			)
		cr.scope(
				getScopesList(client.additionalScopes)
			)
		return cr.build()
	}

	private fun buildClientRegistrationClientCreds(internalClientId: String, client: OAuth2ClientConf): ClientRegistration {
		val cr = ClientRegistration.withRegistrationId(internalClientId)
				.clientId(client.clientId)
				.clientSecret(client.clientSecret)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.tokenUri(cfgAuthSrvIssuerUrl + AuthSrvOauth.URL_PATH_TOKEN)
				.jwkSetUri(cfgAuthSrvIssuerUrl + AuthSrvOauth.URL_PATH_JWK_SET)
		cr.scope(
				getScopesList(client.additionalScopes)
			)
		return cr.build()
	}

	private fun getScopesList(addScopes: List<AuthScope.EnScopes>) : List<String> {
		val scopesList = mutableListOf<String>()
		scopesList.add(AuthScope.EnScopes.OPENID.value)
		addScopes.forEach {
				if (! scopesList.contains(it.value)) {
					scopesList.add(it.value)
				}
			}
		return scopesList
	}
}
