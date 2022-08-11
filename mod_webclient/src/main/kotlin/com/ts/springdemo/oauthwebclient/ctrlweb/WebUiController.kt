package com.ts.springdemo.oauthwebclient.ctrlweb

import com.ts.springdemo.oauthwebclient.repository.CustomClientRegistrationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.ClientAuthorizationException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.oidc.AddressStandardClaim
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.function.client.*
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest


@Controller
class WebUiController(
			@Autowired
			private val webClient: WebClient,
			@Autowired
			private val customClientRegistrationRepository: CustomClientRegistrationRepository,
			@Value("\${custom-app.resource-server.url}")
			private val cfgRscSrvUrl: String,
			@Value("\${custom-app.client-web-app.internal-client-id}")
			private val cfgClientWebAppInternalClientId: String
		) {

	@GetMapping("/")
	@Throws(ResponseStatusException::class)
	fun getUiRoot(authentication: OAuth2AuthenticationToken, model: Model): String {
		if (authentication.principal == null) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Authentication")
		}
		val outUsername: String? = if (authentication.principal is DefaultOidcUser &&
					(authentication.principal as DefaultOidcUser).idToken != null) {
				(authentication.principal as DefaultOidcUser).idToken.fullName
			} else {
				authentication.principal.name
			}
		model["username"] = outUsername ?: "n/a"
		return "ui/root"
	}

	@GetMapping(value = ["/ui/articles"])
	fun getUiArticles(model: Model): String {
		val reqUrl = "${cfgRscSrvUrl}/api/v1/articles"
		val dataArr: Array<String> = getStringArray(reqUrl)
		model["dataArr"] = dataArr
		return "ui/data/articles"
	}

	@GetMapping(value = ["/ui/products"])
	fun getUiProducts(model: Model): String {
		val reqUrl = "${cfgRscSrvUrl}/api/v1/products"
		val dataArr: Array<String> = getStringArray(reqUrl)
		model["dataArr"] = dataArr
		return "/ui/data/products"
	}

	@GetMapping(value = ["/ui/showOidcUserInfo"])
	fun getUiShowOidcUserInfo(model: Model): String {
		val authInfoArr = LinkedHashMap<String, Any>()
		val oauth2ClientReg: ClientRegistration? = customClientRegistrationRepository.findByRegistrationId(cfgClientWebAppInternalClientId)
		if (oauth2ClientReg != null && oauth2ClientReg.authorizationGrantType == AuthorizationGrantType.AUTHORIZATION_CODE) {
			val authenticationPre: Authentication = SecurityContextHolder.getContext().authentication
			if (authenticationPre is OAuth2AuthenticationToken) {
				val authToken: OAuth2AuthenticationToken = authenticationPre
				authInfoArr["Auth User"] = authToken.name
				authInfoArr["Auth Authorities"] = mutableListOf<String>()
				authToken.authorities.forEach {
						@Suppress("UNCHECKED_CAST")
						(authInfoArr["Auth Authorities"] as MutableList<String>).add(it.toString())
					}
				//res["Auth Details"] = authToken.details.toString()
				//res["Auth Principal"] = authToken.principal.toString()
				if (authToken.principal is DefaultOidcUser && (authToken.principal as DefaultOidcUser).userInfo != null) {
					val defaultOidcUser = authToken.principal as DefaultOidcUser
					val oidcUserInfo: OidcUserInfo = defaultOidcUser.userInfo
					authInfoArr["Auth Principal.userInfo.authuser_role"] = getValueOrNull(oidcUserInfo.claims["authuser_role"])
					val oidcIdToken: OidcIdToken = defaultOidcUser.idToken
					val addr: AddressStandardClaim = oidcIdToken.address
					authInfoArr["Auth Principal.idToken.preferredUsername"] = getValueOrNull(oidcIdToken.preferredUsername)
					authInfoArr["Auth Principal.idToken.birthdate"] = getValueOrNull(oidcIdToken.birthdate)
					authInfoArr["Auth Principal.idToken.gender"] = getValueOrNull(oidcIdToken.gender)
					authInfoArr["Auth Principal.idToken.locale"] = getValueOrNull(oidcIdToken.locale)
					authInfoArr["Auth Principal.idToken.nickname"] = getValueOrNull(oidcIdToken.nickName)
					authInfoArr["Auth Principal.idToken.email"] = getValueOrNull(oidcIdToken.email)
					authInfoArr["Auth Principal.idToken.website"] = getValueOrNull(oidcIdToken.website)
					authInfoArr["Auth Principal.idToken.email_verified"] = getValueOrNull(oidcIdToken.emailVerified)
					authInfoArr["Auth Principal.idToken.address.streetAddress"] = getValueOrNull(addr.streetAddress)
					authInfoArr["Auth Principal.idToken.address.postalCode"] = getValueOrNull(addr.postalCode)
					authInfoArr["Auth Principal.idToken.address.locality"] = getValueOrNull(addr.locality)
					authInfoArr["Auth Principal.idToken.address.region"] = getValueOrNull(addr.region)
					authInfoArr["Auth Principal.idToken.address.country"] = getValueOrNull(addr.country)
					authInfoArr["Auth Principal.idToken.address.formatted"] = getValueOrNull(addr.formatted)
					authInfoArr["Auth Principal.idToken.profile"] = getValueOrNull(oidcIdToken.profile)
					authInfoArr["Auth Principal.idToken.phone_number_verified"] = getValueOrNull(oidcIdToken.phoneNumberVerified)
					authInfoArr["Auth Principal.idToken.given_name"] = getValueOrNull(oidcIdToken.givenName)
					authInfoArr["Auth Principal.idToken.middle_name"] = getValueOrNull(oidcIdToken.middleName)
					authInfoArr["Auth Principal.idToken.picture"] = getValueOrNull(oidcIdToken.picture)
					authInfoArr["Auth Principal.idToken.name"] = getValueOrNull(oidcIdToken.fullName)
					authInfoArr["Auth Principal.idToken.family_name"] = getValueOrNull(oidcIdToken.familyName)
					authInfoArr["Auth Principal.idToken.phone_number"] = getValueOrNull(oidcIdToken.phoneNumber)
					authInfoArr["Auth Principal.idToken.zoneInfo"] = getValueOrNull(oidcIdToken.zoneInfo)
					authInfoArr["Auth Principal.idToken.updated_at"] = getValueOrNull(oidcIdToken.updatedAt)
				} else {
					authInfoArr["error"] = "No DefaultOidcUser.userInfo found"
				}
			} else {
				authInfoArr["error"] = "No OAuth2AuthenticationToken found"
			}
		} else if (oauth2ClientReg != null) {
			authInfoArr["error"] = "Client with RegistrationId '$cfgClientWebAppInternalClientId' does not use " +
					AuthorizationGrantType.AUTHORIZATION_CODE.value
		} else {
			authInfoArr["error"] = "Could not find RegistrationId '$cfgClientWebAppInternalClientId'"
		}
		model["authInfoArr"] = authInfoArr
		return "/ui/userinfo/showOidcUserInfo"
	}

	@GetMapping(value = ["/ui/logged_out"])
	fun getUiLoggedOut(request: HttpServletRequest): String {
		request.session.invalidate()
		//
		return "/ui/logged_out"
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun getValueOrNull(value: Any?): String {
		return value?.toString() ?: "NULL"
	}

	@Throws(ClientAuthorizationException::class, ResponseStatusException::class)
	private fun getStringArray(reqUrl: String): Array<String> {
		try {
			val tmpRes: Array<String> = webClient
					.get()
					.uri(reqUrl)
					.attributes(clientRegistrationId(cfgClientWebAppInternalClientId))
					.retrieve()
					.bodyToMono(Array<String>::class.java)
					.block()
					?: throw ResponseStatusException(HttpStatus.NO_CONTENT, "No data received")

			val res = mutableListOf<String>()
			res.addAll(tmpRes.toList())
			return res.toTypedArray()
		} catch (err: WebClientResponseException.Unauthorized) {
			return listOf(
					"UnauthorizedException",
					"method=GET",
					"url=$reqUrl",
					"status=" + err.rawStatusCode,
					"body=" + err.responseBodyAsString,
					"error=" + err.message
				).toTypedArray()
		} catch (err: ClientAuthorizationException) {
			if (err.error.errorCode == "client_authorization_required") {
				throw err
			}
			return listOf(
					"ClientAuthorizationException",
					"method=GET",
					"url=$reqUrl",
					"error=" + err.message,
					"errorCode=" + err.error.errorCode
				).toTypedArray()
		} catch (err: WebClientResponseException.Forbidden) {
			val authentication: Authentication = SecurityContextHolder.getContext().authentication
			return listOf(
					"ForbiddenException",
					"Auth Authorities=" + (if (authentication !is AnonymousAuthenticationToken) { authentication.authorities.toString() } else { "-" }),
					"method=GET",
					"url=$reqUrl",
					"status=" + err.rawStatusCode,
					"body=" + err.responseBodyAsString,
					"error=" + err.message
				).toTypedArray()
		} catch (err: WebClientResponseException) {
			return listOf(
					"WebClientResponseException",
					"method=GET",
					"url=$reqUrl",
					"status=" + err.rawStatusCode,
					"body=" + err.responseBodyAsString,
					"error=" + err.message
				).toTypedArray()
		} catch (err: WebClientException) {
			return listOf(
					"WebClientException",
					"method=GET",
					"url=$reqUrl",
					"error=" + err.message
				).toTypedArray()
		} catch (err: IllegalArgumentException) {
			return listOf(
					"IllegalArgumentException",
					"error=" + err.message
				).toTypedArray()
		}
	}
}
