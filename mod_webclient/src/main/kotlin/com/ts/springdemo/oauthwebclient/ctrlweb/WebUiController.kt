package com.ts.springdemo.oauthwebclient.ctrlweb

import com.ts.springdemo.common.constants.AuthScope
import com.ts.springdemo.common.constants.AuthSrvOauth
import com.ts.springdemo.common.constants.OauthGrantType
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
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.*
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
class WebUiController(
			@Autowired
			private val webClient: WebClient,
			@Autowired
			private val customClientRegistrationRepository: CustomClientRegistrationRepository,
			@Value("\${custom-app.auth-server.provider-issuer-url}")
			private val cfgAuthSrvUrl: String,
			@Value("\${custom-app.resource-server.url}")
			private val cfgRscSrvUrl: String,
			@Value("\${custom-app.client-web-app.url}")
			private val cfgClientWebAppUrl: String,
			@Value("\${custom-app.client-web-app.internal-client-id}")
			private val cfgClientWebAppInternalClientId: String
		) {

	@GetMapping("/")
	@Throws(ResponseStatusException::class)
	fun getUiRoot(authentication: OAuth2AuthenticationToken): String {
		if (authentication.principal == null) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Authentication")
		}
		val username = if (authentication.principal is DefaultOidcUser &&
					(authentication.principal as DefaultOidcUser).idToken != null) {
				(authentication.principal as DefaultOidcUser).idToken.fullName
			} else {
				authentication.principal.name
			}
		return "Hello $username"
	}

	@GetMapping(value = ["/custom/authcode/fetch"])
	@Throws(ResponseStatusException::class)
	fun getCustomAuthcodeFetch(response: HttpServletResponse) {
		val oauth2ClientReg: ClientRegistration = customClientRegistrationRepository.findByRegistrationId(cfgClientWebAppInternalClientId) ?:
				throw ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth2 Client '$cfgClientWebAppInternalClientId' not found")

		val redirUrl = cfgAuthSrvUrl + AuthSrvOauth.URL_PATH_AUTHORIZATION + "?" +
				"response_type=code&" +
				"client_id=${oauth2ClientReg.clientId}&" +
				"redirect_uri=$cfgClientWebAppUrl/custom/authcode/authorized&" +
				"scope=" + AuthScope.EnScopes.OPENID.value
		response.setHeader("Location", redirUrl)
		response.status = 302
	}

	@GetMapping(value = ["/custom/authcode/authorized"])
	@Throws(ResponseStatusException::class)
	fun getCustomAuthcodeAuthorized(code: String?, error: String?): String {
		if (! error.isNullOrEmpty()) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: $error")
		}
		if (code.isNullOrEmpty()) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing code")
		}
		val tokenData = getAccTokenFromAuthServer(code)
		var resStr = "The Code was:<br>" +
				"<pre style='font-size:90%'>$code</pre>"
		if (tokenData.containsKey("error")) {
			resStr += "Error:" +
					"<pre style='font-size:90%'>" + tokenData["error"] + "</pre>"
		} else {
			resStr += "Access Token:<br>" +
					"<pre style='font-size:90%'>" + tokenData["access_token"] + "</pre>" +
					"Refresh Token:<br>" +
					"<pre style='font-size:90%'>" + tokenData["refresh_token"] + "</pre>" +
					"Token Type:<br>" +
					"<pre style='font-size:90%'>" + tokenData["token_type"] + "</pre>" +
					"Expires in:<br>" +
					"<pre style='font-size:90%'>" + tokenData["expires_in"] + "</pre>" +
					"Scope:<br>" +
					"<pre style='font-size:90%'>" + tokenData["scope"] + "</pre>" +
					"ID Token:<br>" +
					"<pre style='font-size:90%'>" + tokenData["id_token"] + "</pre>"
		}
		return resStr
	}

	@GetMapping(value = ["/articles"])
	fun getArticles(): Array<String>? {
		val reqUrl = "${cfgRscSrvUrl}/api/v1/articles"
		return getStringArray(reqUrl)
	}

	@GetMapping(value = ["/products"])
	fun getProducts(): Array<String>? {
		val reqUrl = "${cfgRscSrvUrl}/api/v1/products"
		return getStringArray(reqUrl)
	}

	@GetMapping(value = ["/showOidcUserInfo"])
	fun getShowOidcUserInfo(): Map<String, String> {
		val res = HashMap<String, String>()
		val oauth2ClientReg: ClientRegistration? = customClientRegistrationRepository.findByRegistrationId(cfgClientWebAppInternalClientId)
		if (oauth2ClientReg != null && oauth2ClientReg.authorizationGrantType == AuthorizationGrantType.AUTHORIZATION_CODE) {
			val authenticationPre: Authentication = SecurityContextHolder.getContext().authentication
			if (authenticationPre is OAuth2AuthenticationToken) {
				val authToken: OAuth2AuthenticationToken = authenticationPre
				res["Auth User"] = authToken.name
				res["Auth Authorities"] = authToken.authorities.toString()
				//res["Auth Details"] = authToken.details.toString()
				//res["Auth Principal"] = authToken.principal.toString()
				if (authToken.principal is DefaultOidcUser && (authToken.principal as DefaultOidcUser).userInfo != null) {
					val defaultOidcUser = authToken.principal as DefaultOidcUser
					val oidcUserInfo: OidcUserInfo = defaultOidcUser.userInfo
					res["Auth Principal.userInfo.authuser_role"] = getValueOrNull(oidcUserInfo.claims["authuser_role"])
					val oidcIdToken: OidcIdToken = defaultOidcUser.idToken
					val addr: AddressStandardClaim = oidcIdToken.address
					res["Auth Principal.idToken.preferredUsername"] = getValueOrNull(oidcIdToken.preferredUsername)
					res["Auth Principal.idToken.birthdate"] = getValueOrNull(oidcIdToken.birthdate)
					res["Auth Principal.idToken.gender"] = getValueOrNull(oidcIdToken.gender)
					res["Auth Principal.idToken.locale"] = getValueOrNull(oidcIdToken.locale)
					res["Auth Principal.idToken.nickname"] = getValueOrNull(oidcIdToken.nickName)
					res["Auth Principal.idToken.email"] = getValueOrNull(oidcIdToken.email)
					res["Auth Principal.idToken.website"] = getValueOrNull(oidcIdToken.website)
					res["Auth Principal.idToken.email_verified"] = getValueOrNull(oidcIdToken.emailVerified)
					res["Auth Principal.idToken.address"] = getValueOrNull(addr.streetAddress) + ", " +
							getValueOrNull(addr.postalCode) + ", " + getValueOrNull(addr.locality) + ", " +
							getValueOrNull(addr.region) + ", " + getValueOrNull(addr.country) + ", " +
							getValueOrNull(addr.formatted)
					res["Auth Principal.idToken.profile"] = getValueOrNull(oidcIdToken.profile)
					res["Auth Principal.idToken.phone_number_verified"] = getValueOrNull(oidcIdToken.phoneNumberVerified)
					res["Auth Principal.idToken.given_name"] = getValueOrNull(oidcIdToken.givenName)
					res["Auth Principal.idToken.middle_name"] = getValueOrNull(oidcIdToken.middleName)
					res["Auth Principal.idToken.picture"] = getValueOrNull(oidcIdToken.picture)
					res["Auth Principal.idToken.name"] = getValueOrNull(oidcIdToken.fullName)
					res["Auth Principal.idToken.family_name"] = getValueOrNull(oidcIdToken.familyName)
					res["Auth Principal.idToken.phone_number"] = getValueOrNull(oidcIdToken.phoneNumber)
					res["Auth Principal.idToken.zoneInfo"] = getValueOrNull(oidcIdToken.zoneInfo)
					res["Auth Principal.idToken.updated_at"] = getValueOrNull(oidcIdToken.updatedAt)
				} else {
					res["error"] = "No DefaultOidcUser.userInfo found"
				}
			} else {
				res["error"] = "No OAuth2AuthenticationToken found"
			}
		} else if (oauth2ClientReg != null) {
			res["error"] = "Client with RegistrationId '$cfgClientWebAppInternalClientId' does not use " +
					AuthorizationGrantType.AUTHORIZATION_CODE.value
		} else {
			res["error"] = "Could not find RegistrationId '$cfgClientWebAppInternalClientId'"
		}
		return res
	}

	@GetMapping(value = ["/logout"])
	fun getLogout(request: HttpServletRequest, response: HttpServletResponse) {
		val redirUrl = "${cfgAuthSrvUrl}/logout?" + AuthSrvOauth.URL_PARAM_LOGOUT_PAGE_REDIRECT_TO_WEBCLIENT + "=1"
		response.setHeader("Location", redirUrl)
		response.status = 302
	}

	@GetMapping(value = ["/logged_out"])
	fun getLoggedOut(request: HttpServletRequest, response: HttpServletResponse): String {
		request.session.invalidate()
		//
		return "<h3>You have been logged out.</h3>"
	}

	@GetMapping("/error/forbidden")
	fun getUiErrorForbidden(): String {
		return "Forbidden"
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun getValueOrNull(value: Any?): String {
		return value?.toString() ?: "NULL"
	}

	@Throws(ClientAuthorizationException::class)
	private fun getStringArray(reqUrl: String): Array<String>? {
		val oauth2ClientReg: ClientRegistration? = customClientRegistrationRepository.findByRegistrationId(cfgClientWebAppInternalClientId)
		//
		try {
			val tmpRes: Array<String> = webClient
					.get()
					.uri(reqUrl)
					.attributes(clientRegistrationId(cfgClientWebAppInternalClientId))
					.retrieve()
					.bodyToMono(Array<String>::class.java)
					.block() ?: return null

			val res = mutableListOf<String>()
			res.addAll(tmpRes.toList())
			//
			if (oauth2ClientReg != null) {
				res.add("OAuth2 ClientID=" + oauth2ClientReg.clientId)
			}
			if (oauth2ClientReg != null && oauth2ClientReg.authorizationGrantType == AuthorizationGrantType.AUTHORIZATION_CODE) {
				val authentication: Authentication = SecurityContextHolder.getContext().authentication
				if (authentication is OAuth2AuthenticationToken) {
					res.add("Auth User=" + authentication.name)
					res.add("Auth Authorities=" + authentication.authorities)
					//res.add("Auth Details=" + authentication.details)
					//res.add("Auth Principal=" + authentication.principal)
				}
			}
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
					"OAuth2 ClientID=" + (if (oauth2ClientReg != null) { oauth2ClientReg.clientId } else { "-" }),
					"Auth User=" + (if (authentication !is AnonymousAuthenticationToken) { authentication.name } else { "-" }),
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

	@Throws(ResponseStatusException::class)
	private fun getAccTokenFromAuthServer(code: String): Map<String, String> {
		val tokenEndpointUri: String = cfgAuthSrvUrl + AuthSrvOauth.URL_PATH_TOKEN
		val oauth2ClientReg: ClientRegistration = customClientRegistrationRepository.findByRegistrationId(cfgClientWebAppInternalClientId) ?:
				throw ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth2 Client '$cfgClientWebAppInternalClientId' not found")

		val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
		formData.add("grant_type", OauthGrantType.EnGrantType.AUTH_CODE.value)
		formData.add("code", code)
		formData.add("redirect_uri", "$cfgClientWebAppUrl/custom/authcode/authorized")

		val res: MutableMap<String, String> = HashMap()
		try {
			val srvResp: MutableMap<*, *>? = WebClient.builder()
					.build()
					.post().uri(tokenEndpointUri)
					.headers {headers ->
							headers.setBasicAuth(oauth2ClientReg.clientId, oauth2ClientReg.clientSecret)
						}
					.body(
							BodyInserters.fromFormData(formData)
						)
					.retrieve()
					.bodyToMono(MutableMap::class.java)
					.block()
			if (srvResp != null) {
				@Suppress("UNCHECKED_CAST")
				res.putAll(srvResp as Map<String, String>)
			}
		} catch (err: WebClientResponseException.BadRequest) {
			res["error"] = "BadRequest, URL=${tokenEndpointUri}, Message: ${err.message}"
		} catch (err: WebClientResponseException.Unauthorized) {
			res["error"] = "Unauthorized, URL=$tokenEndpointUri"
		} catch (err: WebClientResponseException.Forbidden) {
			res["error"] = "Forbidden, URL=$tokenEndpointUri"
		} catch (err: WebClientResponseException.NotFound) {
			res["error"] = "NotFound, URL=$tokenEndpointUri"
		} catch (err: WebClientResponseException.MethodNotAllowed) {
			res["error"] = "MethodNotAllowed, URL=$tokenEndpointUri"
		}
		return res
	}
}
