package com.ts.springdemo.authserver.entity

import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.common.constants.AuthRscAcc
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.oauth2.core.Version
import org.springframework.util.Assert
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap


@Suppress("unused")
@Document(collection = "authUser")
class CustomAuthUser private constructor() : Serializable {
	private var id: String = ""
	private var email: String = ""
	private var password: String = ""
	private var roleStr = AuthRole.EnRoles.GRP_NOBODY.value
	private var enabled = false
	private var resourceAccess: Map<String, List<AuthRscAcc.EnMeth>> = HashMap()

	fun getId(): String {
		return id
	}

	fun getEmail(): String {
		return email
	}

	fun getPassword(): String {
		return password
	}

	fun getRoleStr(): String {
		return roleStr
	}

	@Throws(IllegalStateException::class)
	fun getRole(): AuthRole.EnRoles {
		for (roleEn in AuthRole.EnRoles.values()) {
			if (roleEn.value != roleStr) {
				continue
			}
			return roleEn
		}
		throw IllegalStateException("invalid role '${roleStr}'")
	}

	fun getEnabled(): Boolean {
		return enabled
	}

	/**
	 * Get map of Resource-URI-IDs to list of allowed HTTP methods.
	 * E.g. {{'articles' -> GET}, {'products' -> GET, POST}}
	 */
	fun getResourceAccess(): Map<String, List<AuthRscAcc.EnMeth>> {
		return resourceAccess
	}


	companion object {
		private val serialVersionUID = Version.SERIAL_VERSION_UID

		/**
		 * Returns a new [Builder], initialized with the provided registration identifier.
		 *
		 * @param id the identifier for the registration
		 * @return the [Builder]
		 */
		fun withId(id: String): Builder {
			Assert.hasText(id, "id cannot be empty")
			return Builder(id)
		}

		/**
		 * Returns a new [Builder], initialized with a random registration identifier.
		 *
		 * @return the [Builder]
		 */
		fun withRandomId(): Builder {
			return Builder(UUID.randomUUID().toString())
		}

		/**
		 * Returns a new [Builder], initialized with the values from the provided [CustomAuthUser].
		 *
		 * @param authUser the [CustomAuthUser] used for initializing the [Builder]
		 * @return the [Builder]
		 */
		fun from(authUser: CustomAuthUser): Builder {
			return Builder(authUser)
		}

		/**
		 * A builder for [CustomAuthUser].
		 */
		class Builder : Serializable {
			companion object {
				private val serialVersionUID = Version.SERIAL_VERSION_UID
			}
			private var id: String? = null
			private var email: String? = null
			private var password: String? = null
			private var role: AuthRole.EnRoles? = null
			private var enabled: Boolean? = null
			private var resourceAccess: Map<String, List<AuthRscAcc.EnMeth>>? = null

			constructor(id: String) {
				this.id = id
			}

			constructor(authUser: CustomAuthUser) {
				id = authUser.id
				email = authUser.email
				password = authUser.password
				role = authUser.getRole()
				enabled = authUser.enabled
				resourceAccess = authUser.resourceAccess
			}

			fun email(email: String): Builder {
				this.email = email
				return this
			}

			fun password(password: String): Builder {
				this.password = password
				return this
			}

			fun role(role: AuthRole.EnRoles): Builder {
				this.role = role
				return this
			}

			fun enabled(enabled: Boolean): Builder {
				this.enabled = enabled
				return this
			}

			/**
			 * Set map of Resource-URI-IDs to list of allowed HTTP methods.
			 * E.g. {{'articles' -> GET}, {'products' -> GET, POST}}
			 */
			fun resourceAccess(resourceAccess: Map<String, List<AuthRscAcc.EnMeth>>): Builder {
				this.resourceAccess = resourceAccess
				return this
			}

			/**
			 * Builds a new [CustomAuthUser].
			 *
			 * @return a [CustomAuthUser]
			 */
			fun build(): CustomAuthUser {
				Assert.hasText(id, "id cannot be empty")
				Assert.hasText(email, "email cannot be empty")
				Assert.hasText(password, "password cannot be empty")
				Assert.notNull(role, "role cannot be null")
				Assert.notNull(enabled, "enabled cannot be null")
				return create()
			}

			private fun create(): CustomAuthUser {
				val authUser = CustomAuthUser()
				authUser.id = id!!
				authUser.email = email!!
				authUser.password = password!!
				authUser.roleStr = role!!.value
				authUser.enabled = enabled!!
				if (resourceAccess != null) {
					authUser.resourceAccess = resourceAccess as Map<String, List<AuthRscAcc.EnMeth>>
				}
				return authUser
			}
		}
	}
}
