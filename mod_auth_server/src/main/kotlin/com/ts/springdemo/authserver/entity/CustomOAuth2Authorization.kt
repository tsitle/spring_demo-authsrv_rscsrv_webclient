package com.ts.springdemo.authserver.entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.oauth2.core.Version
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.util.Assert
import java.io.Serializable
import java.time.Instant


@Document(collection = "customOauth2Authorization")
data class CustomOAuth2Authorization(
			private var id: String = "",
			private var registeredClientId: String = "",
			private var principalName: String? = null,
			private var authorizationGrantTypeStr: String = "",
			private var attributesStr: String? = null,
			private var state: String? = null,

			private var tokenAuthCodeVal: String? = null,
			private var tokenAuthCodeIa: Instant? = null,
			private var tokenAuthCodeEa: Instant? = null,
			private var tokenAuthCodeMetaStr: String? = null,

			private var tokenAccTokVal: String? = null,
			private var tokenAccTokIa: Instant? = null,
			private var tokenAccTokEa: Instant? = null,
			private var tokenAccTokMetaStr: String? = null,

			private var tokenRefrTokVal: String? = null,
			private var tokenRefrTokIa: Instant? = null,
			private var tokenRefrTokEa: Instant? = null,
			private var tokenRefrTokMetaStr: String? = null,

			private var tokenOidcTokVal: String? = null,
			private var tokenOidcTokIa: Instant? = null,
			private var tokenOidcTokEa: Instant? = null,
			private var tokenOidcTokMetaStr: String? = null
		) : Serializable {

	fun getId(): String {
		return id
	}

	fun getRegisteredClientId(): String {
		return registeredClientId
	}

	fun getPrincipalName(): String? {
		return principalName
	}

	fun getAuthorizationGrantTypeStr(): String {
		return authorizationGrantTypeStr
	}

	fun getAttributesStr(): String? {
		return attributesStr
	}

	fun getState(): String? {
		return state
	}

	fun getTokenAuthCodeVal(): String? {
		return tokenAuthCodeVal
	}

	fun getTokenAuthCodeIa(): Instant? {
		return tokenAuthCodeIa
	}

	fun getTokenAuthCodeEa(): Instant? {
		return tokenAuthCodeEa
	}

	fun getTokenAuthCodeMetaStr(): String? {
		return tokenAuthCodeMetaStr
	}

	fun getTokenAccTokVal(): String? {
		return tokenAccTokVal
	}

	fun getTokenAccTokIa(): Instant? {
		return tokenAccTokIa
	}

	fun getTokenAccTokEa(): Instant? {
		return tokenAccTokEa
	}

	fun getTokenAccTokMetaStr(): String? {
		return tokenAccTokMetaStr
	}

	fun getTokenRefrTokVal(): String? {
		return tokenRefrTokVal
	}

	fun getTokenRefrTokIa(): Instant? {
		return tokenRefrTokIa
	}

	fun getTokenRefrTokEa(): Instant? {
		return tokenRefrTokEa
	}

	fun getTokenRefrTokMetaStr(): String? {
		return tokenRefrTokMetaStr
	}

	fun getTokenOidcTokVal(): String? {
		return tokenOidcTokVal
	}

	fun getTokenOidcTokIa(): Instant? {
		return tokenOidcTokIa
	}

	fun getTokenOidcTokEa(): Instant? {
		return tokenOidcTokEa
	}

	fun getTokenOidcTokMetaStr(): String? {
		return tokenOidcTokMetaStr
	}


	companion object {
		private val serialVersionUID = Version.SERIAL_VERSION_UID

		fun withRegisteredClient(registeredClient: RegisteredClient): Builder {
			return Builder(registeredClient.id)
		}

		/**
		 * A builder for [CustomOAuth2Authorization].
		 */
		class Builder(registeredClientId: String) : Serializable {
			companion object {
				private val serialVersionUID = Version.SERIAL_VERSION_UID
			}
			private var id: String? = null
			private var registeredClientId: String? = registeredClientId
			private var principalName: String? = null
			private var authorizationGrantTypeStr: String? = null
			private var attributesStr: String? = null
			private var state: String? = null

			private var tokenAuthCodeVal: String? = null
			private var tokenAuthCodeIa: Instant? = null
			private var tokenAuthCodeEa: Instant? = null
			private var tokenAuthCodeMetaStr: String? = null

			private var tokenAccTokVal: String? = null
			private var tokenAccTokIa: Instant? = null
			private var tokenAccTokEa: Instant? = null
			private var tokenAccTokMetaStr: String? = null

			private var tokenRefrTokVal: String? = null
			private var tokenRefrTokIa: Instant? = null
			private var tokenRefrTokEa: Instant? = null
			private var tokenRefrTokMetaStr: String? = null

			private var tokenOidcTokVal: String? = null
			private var tokenOidcTokIa: Instant? = null
			private var tokenOidcTokEa: Instant? = null
			private var tokenOidcTokMetaStr: String? = null

			fun id(id: String): Builder {
				this.id = id
				return this
			}

			fun principalName(principalName: String?): Builder {
				this.principalName = principalName
				return this
			}

			fun authorizationGrantTypeStr(authorizationGrantTypeStr: String): Builder {
				this.authorizationGrantTypeStr = authorizationGrantTypeStr
				return this
			}

			fun attributesStr(attributesStr: String?): Builder {
				this.attributesStr = attributesStr
				return this
			}

			fun state(state: String?): Builder {
				this.state = state
				return this
			}

			fun tokenAuthCodeVal(tokenAuthCodeVal: String?): Builder {
				this.tokenAuthCodeVal = tokenAuthCodeVal
				return this
			}

			fun tokenAuthCodeIa(tokenAuthCodeIa: Instant?): Builder {
				this.tokenAuthCodeIa = tokenAuthCodeIa
				return this
			}

			fun tokenAuthCodeEa(tokenAuthCodeEa: Instant?): Builder {
				this.tokenAuthCodeEa = tokenAuthCodeEa
				return this
			}

			fun tokenAuthCodeMetaStr(tokenAuthCodeMetaStr: String?): Builder {
				this.tokenAuthCodeMetaStr = tokenAuthCodeMetaStr
				return this
			}

			fun tokenAccTokVal(tokenAccTokVal: String?): Builder {
				this.tokenAccTokVal = tokenAccTokVal
				return this
			}

			fun tokenAccTokIa(tokenAccTokIa: Instant?): Builder {
				this.tokenAccTokIa = tokenAccTokIa
				return this
			}

			fun tokenAccTokEa(tokenAccTokEa: Instant?): Builder {
				this.tokenAccTokEa = tokenAccTokEa
				return this
			}

			fun tokenAccTokMetaStr(tokenAccTokMetaStr: String?): Builder {
				this.tokenAccTokMetaStr = tokenAccTokMetaStr
				return this
			}

			fun tokenRefrTokVal(tokenRefrTokVal: String?): Builder {
				this.tokenRefrTokVal = tokenRefrTokVal
				return this
			}

			fun tokenRefrTokIa(tokenRefrTokIa: Instant?): Builder {
				this.tokenRefrTokIa = tokenRefrTokIa
				return this
			}

			fun tokenRefrTokEa(tokenRefrTokEa: Instant?): Builder {
				this.tokenRefrTokEa = tokenRefrTokEa
				return this
			}

			fun tokenRefrTokMetaStr(tokenRefrTokMetaStr: String?): Builder {
				this.tokenRefrTokMetaStr = tokenRefrTokMetaStr
				return this
			}

			fun tokenOidcTokVal(tokenOidcTokVal: String?): Builder {
				this.tokenOidcTokVal = tokenOidcTokVal
				return this
			}

			fun tokenOidcTokIa(tokenOidcTokIa: Instant?): Builder {
				this.tokenOidcTokIa = tokenOidcTokIa
				return this
			}

			fun tokenOidcTokEa(tokenOidcTokEa: Instant?): Builder {
				this.tokenOidcTokEa = tokenOidcTokEa
				return this
			}

			fun tokenOidcTokMetaStr(tokenOidcTokMetaStr: String?): Builder {
				this.tokenOidcTokMetaStr = tokenOidcTokMetaStr
				return this
			}


			/**
			 * Builds a new [CustomOAuth2Authorization].
			 *
			 * @return a [CustomOAuth2Authorization]
			 */
			fun build(): CustomOAuth2Authorization {
				Assert.hasText(id, "id cannot be empty")
				Assert.hasText(registeredClientId, "registeredClientId cannot be empty")
				Assert.hasText(authorizationGrantTypeStr, "email cannot be empty")
				return create()
			}

			private fun create(): CustomOAuth2Authorization {
				val custom = CustomOAuth2Authorization()
				custom.id = id!!
				custom.registeredClientId = registeredClientId!!
				custom.principalName = principalName
				custom.authorizationGrantTypeStr = authorizationGrantTypeStr!!
				custom.attributesStr = attributesStr
				custom.state = state

				tokenAuthCodeVal.also { custom.tokenAuthCodeVal = it }  // using "also" here only to stop IntelliJ from complaining about duplicate code
				custom.tokenAuthCodeIa = tokenAuthCodeIa
				custom.tokenAuthCodeEa = tokenAuthCodeEa
				custom.tokenAuthCodeMetaStr = tokenAuthCodeMetaStr

				custom.tokenAccTokVal = tokenAccTokVal
				custom.tokenAccTokIa = tokenAccTokIa
				custom.tokenAccTokEa = tokenAccTokEa
				custom.tokenAccTokMetaStr = tokenAccTokMetaStr

				tokenRefrTokVal.also { custom.tokenRefrTokVal = it }  // using "also" here only to stop IntelliJ from complaining about duplicate code
				custom.tokenRefrTokIa = tokenRefrTokIa
				custom.tokenRefrTokEa = tokenRefrTokEa
				custom.tokenRefrTokMetaStr = tokenRefrTokMetaStr

				tokenOidcTokVal.also { custom.tokenOidcTokVal = it }  // using "also" here only to stop IntelliJ from complaining about duplicate code
				custom.tokenOidcTokIa = tokenOidcTokIa
				custom.tokenOidcTokEa = tokenOidcTokEa
				custom.tokenOidcTokMetaStr = tokenOidcTokMetaStr

				return custom
			}
		}
	}
}
