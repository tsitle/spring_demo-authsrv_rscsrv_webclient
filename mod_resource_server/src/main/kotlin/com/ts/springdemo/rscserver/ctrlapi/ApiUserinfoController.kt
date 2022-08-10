package com.ts.springdemo.rscserver.ctrlapi

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException


@RestController
class ApiUserinfoController {
	private val urlPathAuthServerGetCustomUserinfoOauth = "/api/v1/userinfo/oauth"

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	/** This endpoint can be reached with an OAuth2 Authorization Code or Client Credentials grant */
	@PostMapping("/api/v1/userinfo")
	fun postUserinfo(queryUserEmail: String?): Map<String, String> {
		if (queryUserEmail == null || queryUserEmail.isEmpty()) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing queryUserEmail")
		}
		return getUserInfoFromAuthServer(queryUserEmail)
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun getUserInfoFromAuthServer(userEmail: String): Map<String, String> {
		val authenticationPre = SecurityContextHolder.getContext().authentication ?:
				throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "OAuth2 Login required")
		if (authenticationPre is AnonymousAuthenticationToken) {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "OAuth2 Login required")
		}
		if (authenticationPre !is JwtAuthenticationToken) {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid Authentication")
		}

		val authentication: JwtAuthenticationToken = authenticationPre
		val principal: Jwt = authentication.principal as Jwt
		val authorizedClientsAccessTokenValue: String = principal.tokenValue
		val userInfoEndpointUri: String = principal.getClaimAsString("iss") + urlPathAuthServerGetCustomUserinfoOauth

		val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
		formData.add("queryUserEmail", userEmail)

		val res: MutableMap<String, String> = HashMap()
		try {
			val srvResp: MutableMap<*, *>? = WebClient.builder()
					.build()
					.post().uri(userInfoEndpointUri)
					.headers { headers ->
							headers.setBearerAuth(authorizedClientsAccessTokenValue)
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
			res["error"] = "BadRequest, URL=$userInfoEndpointUri"
		} catch (err: WebClientResponseException.Unauthorized) {
			res["error"] = "Unauthorized, URL=$userInfoEndpointUri"
		} catch (err: WebClientResponseException.Forbidden) {
			res["error"] = "Forbidden, URL=$userInfoEndpointUri"
		} catch (err: WebClientResponseException.NotFound) {
			res["error"] = "NotFound, URL=$userInfoEndpointUri"
		} catch (err: WebClientResponseException.MethodNotAllowed) {
			res["error"] = "MethodNotAllowed, URL=$userInfoEndpointUri"
		}
		return res
	}
}
