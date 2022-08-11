package com.ts.springdemo.oauthwebclient.ctrlweb

import com.ts.springdemo.common.constants.AuthSrvOauth
import com.ts.springdemo.common.constants.OauthGrantType
import com.ts.springdemo.oauthwebclient.repository.CustomClientRegistrationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.*
import org.springframework.web.server.ResponseStatusException


@Controller
class WebAuthRespController(
			@Autowired
			private val customClientRegistrationRepository: CustomClientRegistrationRepository,
			@Value("\${custom-app.auth-server.provider-issuer-url}")
			private val cfgAuthSrvUrl: String,
			@Value("\${custom-app.client-web-app.url}")
			private val cfgClientWebAppUrl: String,
			@Value("\${custom-app.client-web-app.internal-client-id}")
			private val cfgClientWebAppInternalClientId: String
		) {

	@GetMapping(value = ["/custom/authcode/authorized"])
	@Throws(ResponseStatusException::class)
	fun getAuthCustomAuthcodeAuthorized(code: String?, error: String?, model: Model): String {
		if (! error.isNullOrEmpty()) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: $error")
		}
		if (code.isNullOrEmpty()) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing code")
		}
		val tokenData: Map<String, String> = getAccTokenFromAuthServer(code)
		model["error"] = tokenData["error"] ?: ""
		model["code"] = code
		model["td_access_token"] = tokenData["access_token"] ?: ""
		model["td_refresh_token"] = tokenData["refresh_token"] ?: ""
		model["td_token_type"] = tokenData["token_type"] ?: ""
		model["td_expires_in"] = tokenData["expires_in"].toString() ?: ""
		model["td_scope"] = tokenData["scope"] ?: ""
		model["td_id_token"] = tokenData["id_token"] ?: ""
		return "authcode/authorized"
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@Throws(ResponseStatusException::class)
	private fun getAccTokenFromAuthServer(code: String): Map<String, String> {
		val tokenEndpointUri: String = cfgAuthSrvUrl + AuthSrvOauth.URL_PATH_TOKEN
		val oauth2ClientReg: ClientRegistration = customClientRegistrationRepository.findByRegistrationId(cfgClientWebAppInternalClientId)
				?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth2 Client '$cfgClientWebAppInternalClientId' not found")

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
