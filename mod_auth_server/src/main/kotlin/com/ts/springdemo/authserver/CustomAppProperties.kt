package com.ts.springdemo.authserver

import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.common.constants.AuthRscAcc
import com.ts.springdemo.common.constants.AuthScope
import com.ts.springdemo.common.constants.OauthGrantType
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


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
			var resourceIdToUrlPaths: Map<String, ResourceIdToUrlPathsEntry>,
			val authServer: AuthServer,
			val clientWebApp: ClientWebApp
		): InitializingBean {

	data class ResourceIdToUrlPathsEntry(
				var srv: AuthRscAcc.EnSrv,
				var paths: List<String>
			)

	data class AuthServer(
				val providerIssuerUrl: String,
				val enablePostmanCallback: Boolean = false,
				val enableErrorMessagesInWebErrorController: Boolean = false,
				val db: DbConf,
				var users: Map<String, AuthUserConf>,
				var oauth2Clients: Map<String, OAuth2ClientConf>
			) {

		data class DbConf(
					val pruneCollections: Boolean = false,
					val initCollections: Boolean = true
				)

		data class AuthUserConf(
					var role: AuthRole.EnRoles,
					var email: String,
					var password: String,
					var enabled: Boolean = true,
					var oidcInfo: OidcInfo,
					var rolesResourceAccess: Map<String, ResourceAccessUriMethods>?
				) {

			data class OidcInfo(
						/** The user's family name */
						var familyName: String,
						/** The user's first name */
						var givenName: String,
						/** The user's middle name */
						var middleName: String? = null,
						/** The user's nickname */
						var nickName: String? = null,
						/** The user's profile URL */
						var profileUrl: String? = null,
						/** The user's picture URL (avatar) */
						var pictureUrl: String? = null,
						/** The user's website URL */
						var websiteUrl: String? = null,
						/** Has the user's email address been verified? */
						var emailVerified: Boolean = false,
						/** The user's gender (e.g. 'male' or 'female') */
						var gender: String? = null,
						/** The user's birthdate in string representation (e.g. '1970-01-23') */
						var birthdate: String? = null,
						/** The user's timezone in string representation (e.g. 'Europe/Berlin') */
						var timezone: String? = null,
						/** The user's locale (e.g. 'en-US') */
						var locale: String? = null,
						/** The user's phone number */
						var phoneNumber: String? = null,
						/** Has the user's phone number been verified? */
						var phoneNumberVerified: Boolean = false,
						/** The user's full address */
						var address: UserAddress? = null
					) {

				fun getBirthdateAsDate(): Date? {
					return if (birthdate == null) {
							null
						} else {
							SimpleDateFormat("yyyy-MM-dd").parse(birthdate)
						}
				}

				data class UserAddress(
							/** The user's full street address, which may include house number, street name, P.O. Box, etc. */
							var streetAddress: String? = null,
							/** The user's zip code or postal code */
							var postalCode: String? = null,
							/** The user's city or locality */
							var locality: String? = null,
							/** The user's state, province, prefecture, or region */
							var region: String? = null,
							/** The user's country */
							var country: String? = null
						)
			}
		}

		data class OAuth2ClientConf(
					var clientSecret: String,
					var additionalScopes: MutableList<AuthScope.EnScopes> = mutableListOf(),
					var authorizationGrantTypes: List<OauthGrantType.EnGrantType>,
					var scopesResourceAccess: Map<String, ResourceAccessUriMethods>?
				)

		data class ResourceAccessUriMethods(
					var methods: List<AuthRscAcc.EnMeth>
				)
	}

	data class ClientWebApp(
				val url: String,
				val oauthCallbackPagesTemplates: List<String>,
				val loggedOutPage: String
			)

	override fun afterPropertiesSet() {
		validate()
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun validate() {
		val availRscIds: List<String> = resourceIdToUrlPaths.keys.toList()
		authServer.users.values.forEach { validateUser(it, availRscIds) }
		authServer.oauth2Clients.forEach { validateClient(it.key, it.value, availRscIds) }
	}

	companion object {
		private fun validateEmailAddress(emailAddress: String): Boolean {
			val regexPattern = ("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
					+ "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
			return Pattern.matches(regexPattern, emailAddress)
		}

		@Throws(IllegalStateException::class)
		private fun validateUser(user: AuthServer.AuthUserConf, availRscIds: List<String>) {
			if (user.email.isEmpty()) {
				throw IllegalStateException("email must not be empty")
			}
			if (! validateEmailAddress(user.email)) {
				throw IllegalStateException("email must be valid email address (invalid: '" + user.email + "')")
			}
			if (user.password.isEmpty()) {
				throw IllegalStateException("password must not be empty")
			}
			if (! user.rolesResourceAccess.isNullOrEmpty()) {
				validateRscIds("AuthUser '${user.email}'", user.rolesResourceAccess!!.keys.toList(), availRscIds)
			}
		}

		private fun validateClient(clientId: String, clientConf: AuthServer.OAuth2ClientConf, availRscIds: List<String>) {
			if (! clientConf.scopesResourceAccess.isNullOrEmpty()) {
				validateRscIds("OAuthClient '${clientId}'", clientConf.scopesResourceAccess!!.keys.toList(), availRscIds)
			}
		}

		@Throws(IllegalStateException::class)
		private fun validateRscIds(errDesc: String, usedRscIds: List<String>, availRscIds: List<String>) {
			usedRscIds.forEach {
				if (! availRscIds.contains(it)) {
					throw IllegalStateException("invalid Resource ID '${it}' in $errDesc")
				}
			}
		}
	}
}
