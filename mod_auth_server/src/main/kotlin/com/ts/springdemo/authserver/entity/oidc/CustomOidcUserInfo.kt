package com.ts.springdemo.authserver.entity.oidc

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.oauth2.core.Version
import org.springframework.util.Assert
import java.io.Serializable
import java.time.Instant
import java.util.*


@Suppress("unused")
@Document(collection = "oidcUserInfo")
class CustomOidcUserInfo private constructor() : Serializable {
	private var id: String = ""
	private var authUserId: String = ""
	private var familyName: String? = null
	private var givenName: String? = null
	private var middleName: String? = null
	private var nickName: String? = null
	private var profileUrl: String? = null
	private var pictureUrl: String? = null
	private var websiteUrl: String? = null
	private var emailVerified: Boolean = false
	private var gender: String? = null
	private var birthdate: Date? = null
	private var timezone: String? = null
	private var locale: String? = null
	private var phoneNumber: String? = null
	private var phoneNumberVerified: Boolean = false
	private var addressStreetAddress: String? = null
	private var addressPostalCode: String? = null
	private var addressLocality: String? = null
	private var addressRegion: String? = null
	private var addressCountry: String? = null
	private var updatedAt: Instant? = null

	/** Get this record's unique ID */
	fun getId(): String {
		return id
	}

	/** Get this record's related AuthUser's record ID */
	fun getAuthUserId(): String {
		return authUserId
	}

	/** Get the user's family name */
	fun getFamilyName(): String? {
		return familyName
	}

	/** Get the user's first name */
	fun getGivenName(): String? {
		return givenName
	}

	/** Get the user's first and last name (aka given and family name) */
	fun getName(): String {
		var res = givenName ?: ""
		res += if (res.isEmpty() || familyName == null || (familyName as String).isEmpty()) { "" } else { " " }
		res += familyName ?: ""
		return res
	}

	/** Get the user's middle name */
	fun getMiddleName(): String? {
		return middleName
	}

	/** Get the user's nickname */
	fun getNickName(): String? {
		return nickName
	}

	/** Get the user's profile URL */
	fun getProfileUrl(): String? {
		return profileUrl
	}

	/** Get the user's picture URL (avatar) */
	fun getPictureUrl(): String? {
		return pictureUrl
	}

	/** Get the user's website URL */
	fun getWebsiteUrl(): String? {
		return websiteUrl
	}

	/** Has the user's email address been verified? */
	fun getEmailVerified(): Boolean {
		return emailVerified
	}

	/** Get the user's gender (e.g. 'male' or 'female') */
	fun getGender(): String? {
		return gender
	}

	/** Get the user's birthdate (e.g. 1970-01-23) */
	fun getBirthdate(): Date? {
		return birthdate
	}

	/** Get the user's timezone in string representation (e.g. 'Europe/Berlin') */
	fun getTimezone(): String? {
		return timezone
	}

	/** Get the user's locale (e.g. 'en-US') */
	fun getLocale(): String? {
		return locale
	}

	/** Get the user's phone number */
	fun getPhoneNumber(): String? {
		return phoneNumber
	}

	/** Has the user's phone number been verified? */
	fun getPhoneNumberVerified(): Boolean {
		return phoneNumberVerified
	}

	/** Get the user's full street address, which may include house number, street name, P.O. Box, etc. */
	fun getAddressStreetAddress(): String? {
		return addressStreetAddress
	}

	/** Get the user's zip code or postal code */
	fun getAddressPostalCode(): String? {
		return addressPostalCode
	}

	/** Get the user's city or locality */
	fun getAddressLocality(): String? {
		return addressLocality
	}

	/** Get the user's state, province, prefecture, or region */
	fun getAddressRegion(): String? {
		return addressRegion
	}

	/** Get the user's country */
	fun getAddressCountry(): String? {
		return addressCountry
	}

	/** Get the date/time this record has been updated at */
	fun getUpdatedAt(): Instant? {
		return updatedAt
	}


	companion object {
		private val serialVersionUID = Version.SERIAL_VERSION_UID

		fun withId(id: String): Builder {
			Assert.hasText(id, "id cannot be empty")
			return Builder(id)
		}

		fun withRandomId(): Builder {
			return Builder(UUID.randomUUID().toString())
		}

		fun from(customOidcUserInfo: CustomOidcUserInfo): Builder {
			return Builder(customOidcUserInfo)
		}

		class Builder : Serializable {
			companion object {
				private val serialVersionUID = Version.SERIAL_VERSION_UID
			}
			private var id: String? = null
			private var authUserId: String? = null
			private var familyName: String? = null
			private var givenName: String? = null
			private var middleName: String? = null
			private var nickName: String? = null
			private var profileUrl: String? = null
			private var pictureUrl: String? = null
			private var websiteUrl: String? = null
			private var emailVerified: Boolean? = null
			private var gender: String? = null
			private var birthdate: Date? = null
			private var timezone: String? = null
			private var locale: String? = null
			private var phoneNumber: String? = null
			private var phoneNumberVerified: Boolean? = null
			private var addressStreetAddress: String? = null
			private var addressPostalCode: String? = null
			private var addressLocality: String? = null
			private var addressRegion: String? = null
			private var addressCountry: String? = null
			private var updatedAt: Instant? = null

			constructor(id: String) {
				this.id = id
			}

			constructor(customOidcUserInfo: CustomOidcUserInfo) {
				id = customOidcUserInfo.id
				authUserId = customOidcUserInfo.authUserId
				familyName = customOidcUserInfo.familyName
				givenName = customOidcUserInfo.givenName
				middleName = customOidcUserInfo.middleName
				nickName = customOidcUserInfo.nickName
				profileUrl = customOidcUserInfo.profileUrl
				pictureUrl = customOidcUserInfo.pictureUrl
				websiteUrl = customOidcUserInfo.websiteUrl
				emailVerified = customOidcUserInfo.emailVerified
				gender = customOidcUserInfo.gender
				birthdate = customOidcUserInfo.birthdate
				timezone = customOidcUserInfo.timezone
				locale = customOidcUserInfo.locale
				phoneNumber = customOidcUserInfo.phoneNumber
				phoneNumberVerified = customOidcUserInfo.phoneNumberVerified
				addressStreetAddress = customOidcUserInfo.addressStreetAddress
				addressPostalCode = customOidcUserInfo.addressPostalCode
				addressLocality = customOidcUserInfo.addressLocality
				addressRegion = customOidcUserInfo.addressRegion
				addressCountry = customOidcUserInfo.addressCountry
				updatedAt = customOidcUserInfo.updatedAt
			}

			/** Set this record's related AuthUser's record ID */
			fun authUserId(authUserId: String): Builder {
				this.authUserId = authUserId
				return this
			}

			/** Set the user's family name */
			fun familyName(familyName: String?): Builder {
				this.familyName = familyName
				return this
			}

			/** Set the user's first name */
			fun givenName(givenName: String?): Builder {
				this.givenName = givenName
				return this
			}

			/** Set the user's middle name */
			fun middleName(middleName: String?): Builder {
				this.middleName = middleName
				return this
			}

			/** Set the user's nickname */
			fun nickName(nickName: String?): Builder {
				this.nickName = nickName
				return this
			}

			/** Set the user's profile URL */
			fun profileUrl(profileUrl: String?): Builder {
				this.profileUrl = profileUrl
				return this
			}

			/** Set the user's picture URL (avatar) */
			fun pictureUrl(pictureUrl: String?): Builder {
				this.pictureUrl = pictureUrl
				return this
			}

			/** Set the user's website URL */
			fun websiteUrl(websiteUrl: String?): Builder {
				this.websiteUrl = websiteUrl
				return this
			}

			/** Set whether the user's email address has been verified */
			fun emailVerified(emailVerified: Boolean): Builder {
				this.emailVerified = emailVerified
				return this
			}

			/** Set the user's gender (e.g. 'male' or 'female') */
			fun gender(gender: String?): Builder {
				this.gender = gender
				return this
			}

			/** Set the user's birthdate (e.g. 1970-01-23) */
			fun birthdate(birthdate: Date?): Builder {
				this.birthdate = birthdate
				return this
			}

			/** Set the user's timezone in string representation (e.g. 'Europe/Berlin') */
			fun timezone(timezone: String?): Builder {
				this.timezone = timezone
				return this
			}

			/** Set the user's locale (e.g. 'en-US') */
			fun locale(locale: String?): Builder {
				this.locale = locale
				return this
			}

			/** Set the user's phone number */
			fun phoneNumber(phoneNumber: String?): Builder {
				this.phoneNumber = phoneNumber
				return this
			}

			/** Set whether the user's phone number has been verified */
			fun phoneNumberVerified(phoneNumberVerified: Boolean): Builder {
				this.phoneNumberVerified = phoneNumberVerified
				return this
			}

			/** Set the user's full street address, which may include house number, street name, P.O. Box, etc. */
			fun addressStreetAddress(addressStreetAddress: String?): Builder {
				this.addressStreetAddress = addressStreetAddress
				return this
			}

			/** Set the user's zip code or postal code */
			fun addressPostalCode(addressPostalCode: String?): Builder {
				this.addressPostalCode = addressPostalCode
				return this
			}

			/** Set the user's city or locality */
			fun addressLocality(addressLocality: String?): Builder {
				this.addressLocality = addressLocality
				return this
			}

			/** Set the user's state, province, prefecture, or region */
			fun addressRegion(addressRegion: String?): Builder {
				this.addressRegion = addressRegion
				return this
			}

			/** Set the user's country */
			fun addressCountry(addressCountry: String?): Builder {
				this.addressCountry = addressCountry
				return this
			}

			/** Set the date/time this record has been updated at */
			fun updatedAt(updatedAt: Instant?): Builder {
				this.updatedAt = updatedAt
				return this
			}

			/**
			 * Builds a new [CustomOidcUserInfo].
			 *
			 * @return a [CustomOidcUserInfo]
			 */
			fun build(): CustomOidcUserInfo {
				Assert.hasText(id, "id cannot be empty")
				Assert.hasText(authUserId, "authUserId cannot be empty")
				return create()
			}

			private fun create(): CustomOidcUserInfo {
				val customOidcUserInfo = CustomOidcUserInfo()
				customOidcUserInfo.id = id!!
				customOidcUserInfo.authUserId = authUserId!!
				customOidcUserInfo.familyName = familyName
				customOidcUserInfo.givenName = givenName
				customOidcUserInfo.middleName = middleName
				customOidcUserInfo.nickName = nickName
				customOidcUserInfo.profileUrl = profileUrl
				customOidcUserInfo.pictureUrl = pictureUrl
				customOidcUserInfo.websiteUrl = websiteUrl
				customOidcUserInfo.emailVerified = emailVerified ?: false
				customOidcUserInfo.gender = gender
				customOidcUserInfo.birthdate = birthdate
				customOidcUserInfo.timezone = timezone
				customOidcUserInfo.locale = locale
				customOidcUserInfo.phoneNumber = phoneNumber
				customOidcUserInfo.phoneNumberVerified = phoneNumberVerified ?: false
				customOidcUserInfo.addressStreetAddress = addressStreetAddress
				customOidcUserInfo.addressPostalCode = addressPostalCode
				customOidcUserInfo.addressLocality = addressLocality
				customOidcUserInfo.addressRegion = addressRegion
				customOidcUserInfo.addressCountry = addressCountry
				customOidcUserInfo.updatedAt = updatedAt
				return customOidcUserInfo
			}
		}
	}
}
