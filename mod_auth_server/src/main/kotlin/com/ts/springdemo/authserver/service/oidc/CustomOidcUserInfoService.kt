package com.ts.springdemo.authserver.service.oidc

import com.ts.springdemo.common.constants.AuthScope
import com.ts.springdemo.authserver.entity.CustomAuthUser
import com.ts.springdemo.authserver.entity.oidc.CustomOidcUserInfo
import com.ts.springdemo.authserver.repository.CustomAuthUserRepository
import com.ts.springdemo.authserver.repository.oidc.CustomOidcUserInfoRepository
import com.ts.springdemo.authserver.service.AccessTokenEnhancingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.oidc.DefaultAddressStandardClaim
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat


@Service
class CustomOidcUserInfoService(
			@Autowired
			private val customAuthUserRepository: CustomAuthUserRepository,
			@Autowired
			private val customOidcUserInfoRepository: CustomOidcUserInfoRepository,
			@Autowired
			private val accessTokenEnhancingService: AccessTokenEnhancingService
		) {

	companion object {
		// Field names from DefaultAddressStandardClaim
		private const val ADDR_FIELD_FORMATTED = "formatted"
		private const val ADDR_FIELD_STREET_ADDRESS = "street_address"
		private const val ADDR_FIELD_LOCALITY = "locality"
		private const val ADDR_FIELD_REGION = "region"
		private const val ADDR_FIELD_POSTAL_CODE = "postal_code"
		private const val ADDR_FIELD_COUNTRY = "country"
	}

	fun loadUserForIdToken(username: String?, authorizedScopes: Set<String>): OidcUserInfo? {
		val authUser: CustomAuthUser = customAuthUserRepository.findByEmail(username) ?:
				return null
		val customOidcUserInfo: CustomOidcUserInfo = customOidcUserInfoRepository.findByAuthUserId(authUser.getId()) ?:
				return null
		val defaultOidcUserInfo: OidcUserInfo = convertCustomToDefaultOidcUserInfo(
				authUser, customOidcUserInfo, authorizedScopes
			)
		return OidcUserInfo(defaultOidcUserInfo.claims)
	}

	fun getMappedPrincipalClaimsForUserInfoInJwtAuthToken(jwtAuthenticationToken: JwtAuthenticationToken): OidcUserInfo {
		val username: String? = jwtAuthenticationToken.name
		val authUser: CustomAuthUser? = if (! username.isNullOrEmpty()) {
				customAuthUserRepository.findByEmail(username)
			} else {
				null
			}
		val additionalClaims: Map<String, Any> = accessTokenEnhancingService.getAdditionalClaims(authUser, emptyList())
		return OidcUserInfo.builder()
				.claims { remClaims ->
						remClaims.putAll(jwtAuthenticationToken.token.claims)
						remClaims["nbf"] = jwtAuthenticationToken.token.claims["nbf"].toString()
						remClaims["exp"] = jwtAuthenticationToken.token.claims["exp"].toString()
						remClaims["iat"] = jwtAuthenticationToken.token.claims["iat"].toString()
						remClaims.putAll(additionalClaims)
					}
				.build()
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun convertCustomToDefaultOidcUserInfo(
				authUser: CustomAuthUser,
				customOidcUserInfo: CustomOidcUserInfo,
				authorizedScopes: Set<String>
			): OidcUserInfo {
		val addressMap = convertAddressToMap(customOidcUserInfo)
		val birthdayStr: String? = if (customOidcUserInfo.getBirthdate() != null) {
				SimpleDateFormat("yyyy-MM-dd").format(customOidcUserInfo.getBirthdate())
			} else {
				null
			}
		val updatedAtStr: String? = if (customOidcUserInfo.getUpdatedAt() != null) { customOidcUserInfo.getUpdatedAt().toString() } else { null }
		val emailStr = authUser.getEmail()
		val phoneNumberVerifiedStr = if (customOidcUserInfo.getPhoneNumberVerified()) { "true" } else { "false" }

		val builder = OidcUserInfo.builder()
				.subject(emailStr)
		if (authorizedScopes.contains(AuthScope.EnScopes.OIDC_PROFILE.value)) {
			/**
			 * com.nimbusds.openid.connect.sdk.OIDCScopeValue.OIDCScopeValue PROFILE =
			 *   new OIDCScopeValue("profile", new String[]{
			 *     "name", "given_name", "middle_name", "family_name", "nickname", "preferred_username",
			 *     "profile", "picture", "website", "gender", "birthdate", "zoneinfo", "locale", "updated_at"});
			*/
			builder
					.name(customOidcUserInfo.getName())
					.givenName(customOidcUserInfo.getGivenName())
					.middleName(customOidcUserInfo.getMiddleName())
					.familyName(customOidcUserInfo.getFamilyName())
					.nickname(customOidcUserInfo.getNickName())
					.preferredUsername(emailStr)
					.profile(customOidcUserInfo.getProfileUrl())
					.picture(customOidcUserInfo.getPictureUrl())
					.website(customOidcUserInfo.getWebsiteUrl())
					.gender(customOidcUserInfo.getGender())
					.birthdate(birthdayStr)
					.zoneinfo(customOidcUserInfo.getTimezone())
					.locale(customOidcUserInfo.getLocale())
					.updatedAt(updatedAtStr)
		}
		if (authorizedScopes.contains(AuthScope.EnScopes.OIDC_EMAIL.value)) {
			/**
			 * com.nimbusds.openid.connect.sdk.OIDCScopeValue.OIDCScopeValue EMAIL =
			 *   new OIDCScopeValue("email", new String[]{"email", "email_verified"});
			 */
			builder
					.email(emailStr)
					.emailVerified(customOidcUserInfo.getEmailVerified())
		}
		if (authorizedScopes.contains(AuthScope.EnScopes.OIDC_ADDRESS.value)) {
			/**
			 * com.nimbusds.openid.connect.sdk.OIDCScopeValue.OIDCScopeValue ADDRESS =
			 *   new OIDCScopeValue("address", new String[]{"address"});
			 */
			builder
					.claim("address", addressMap)
		}
		if (authorizedScopes.contains(AuthScope.EnScopes.OIDC_PHONE.value)) {
			/**
			 * com.nimbusds.openid.connect.sdk.OIDCScopeValue.OIDCScopeValue PHONE =
			 *   new OIDCScopeValue("phone", new String[]{"phone_number", "phone_number_verified"});
			 */
			builder
					.phoneNumber(customOidcUserInfo.getPhoneNumber())
					.phoneNumberVerified(phoneNumberVerifiedStr)
		}
		return builder.build()
	}

	private fun convertAddressToMap(customOidcUserInfo: CustomOidcUserInfo): Map<String, String>? {
		val addressObj = DefaultAddressStandardClaim.Builder()
				.streetAddress(customOidcUserInfo.getAddressStreetAddress())
				.postalCode(customOidcUserInfo.getAddressPostalCode())
				.locality(customOidcUserInfo.getAddressLocality())
				.region(customOidcUserInfo.getAddressRegion())
				.country(customOidcUserInfo.getAddressCountry())
				.formatted(null)  // full mailing address, formatted for display
				.build()
		val res = HashMap<String, String>()
		var haveOne = false
		if (addressObj.streetAddress != null) {
			res[ADDR_FIELD_STREET_ADDRESS] = addressObj.streetAddress
			haveOne = true
		}
		if (addressObj.postalCode != null) {
			res[ADDR_FIELD_POSTAL_CODE] = addressObj.postalCode
			haveOne = true
		}
		if (addressObj.locality != null) {
			res[ADDR_FIELD_LOCALITY] = addressObj.locality
			haveOne = true
		}
		if (addressObj.region != null) {
			res[ADDR_FIELD_REGION] = addressObj.region
			haveOne = true
		}
		if (addressObj.country != null) {
			res[ADDR_FIELD_COUNTRY] = addressObj.country
			haveOne = true
		}
		if (addressObj.formatted != null) {
			res[ADDR_FIELD_FORMATTED] = addressObj.formatted
			haveOne = true
		}
		return if (! haveOne) { null } else { res }
	}
}
