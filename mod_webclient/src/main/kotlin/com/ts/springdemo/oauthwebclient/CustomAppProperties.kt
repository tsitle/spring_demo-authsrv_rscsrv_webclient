package com.ts.springdemo.oauthwebclient

import com.ts.springdemo.common.constants.AuthScope
import com.ts.springdemo.common.constants.OauthGrantType
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


/**
 * In IntelliJ IDEA:
 *   - Make sure Spring Boot plugin in enabled in menu File | Settings | Plugins | Spring Boot
 *   - Enable annotation processing via menu File | Settings | Build, Execution, Deployment | Compiler | Annotation Processors | Enable annotation processing
 * Since Kapt is not yet integrated in IDEA, you need to run manually the command
 *   $ ./gradlew kaptKotlin
 * to generate the metadata
 */
@ConstructorBinding
@ConfigurationProperties("custom-app")
data class CustomAppProperties(
			var authServer: AuthServer,
			var resourceServer: ResourceServer,
			var clientWebApp: ClientWebApp
		): InitializingBean {

	data class AuthServer(val providerIssuerUrl: String)

	data class ResourceServer(val url: String)

	data class ClientWebApp(
				val internalClientId: String,
				val url: String,
				var oauth2Clients: Map<String, OAuth2ClientConf> = HashMap()
			) {

		data class OAuth2ClientConf(
					val clientId: String,
					val clientSecret: String = "",
					var additionalScopes: MutableList<AuthScope.EnScopes> = mutableListOf(),
					val authorizationGrantType: OauthGrantType.EnGrantType = OauthGrantType.EnGrantType.AUTH_CODE
				)
	}

	override fun afterPropertiesSet() {
		validate()
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun validate() {
		clientWebApp.oauth2Clients.values.forEach(CustomAppProperties::validateClients)
		//
		if (! clientWebApp.oauth2Clients.keys.contains(clientWebApp.internalClientId)) {
			throw IllegalStateException("Registration ID '" + clientWebApp.internalClientId + "' not found in oauth2Clients")
		}
	}

	companion object {
		private fun validateClients(client: ClientWebApp.OAuth2ClientConf) {
			if (client.clientId.isEmpty()) {
				throw IllegalStateException("clientId must not be empty")
			}
			/*if (client.authorizationGrantType != OauthGrantType.EnGrantType.AUTH_CODE) {
				println("-- Warning: client '" + client.clientId + "' needs Grant Type " + OauthGrantType.EnGrantType.AUTH_CODE)
			}*/
		}
	}
}
