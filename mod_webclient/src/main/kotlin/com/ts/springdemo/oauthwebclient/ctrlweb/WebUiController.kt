package com.ts.springdemo.oauthwebclient.ctrlweb

import com.ts.springdemo.common.entityapi.*
import com.ts.springdemo.oauthwebclient.entityform.FormDataArticle
import com.ts.springdemo.oauthwebclient.entityform.FormDataProduct
import com.ts.springdemo.oauthwebclient.repository.CustomClientRegistrationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.codec.DecodingException
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
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid


@Controller
class WebUiController(
			@Autowired
			private val webClientRscSrv: WebClient,
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

	// -----------------------------------------------------------------------------------------------------------------

	@GetMapping(value = ["/ui/articles/show"])
	fun getUiArticlesShow(model: Model): String {
		val reqUrl = "${cfgRscSrvUrl}/api/v1/articles"
		val apiResp: ApiCrudResponseRead<ApiDataArticle> = getRecordArrayFromRscSrv(reqUrl)
		model["dataArr"] = apiResp.getElems()
		return "ui/data/articles_show"
	}

	@GetMapping(value = ["/ui/articles/create"])
	fun getUiArticlesCreate(model: Model): String {
		model["targetUrl"] = "/ui/articles/create"
		model["userId"] = getAuthUserId()
		model["formDataArticle"] = FormDataArticle()
		return "ui/data/articles_create"
	}

	@PostMapping(value = ["/ui/articles/create"])
	fun postUiArticlesCreate(
				@Valid @ModelAttribute fdArticle: FormDataArticle,
				errors: BindingResult,
				model: Model,
				redirectAttrs: RedirectAttributes
			): String {
		var hasErrors = errors.hasErrors()

		if (! hasErrors) {
			val endpointUri = "$cfgRscSrvUrl/api/v1/articles"
			val apiDataArticle = ApiDataArticle.withId("bogus")
					.userId("bogus")
					.lines(
							fdArticle.linesStr!!.lines()
						)
					.build()
			val apiResp: ApiCrudResponseCreate = createRecordOnRscSrv(apiDataArticle, endpointUri)
			if (! apiResp.getOk()) {
				hasErrors = true
				model["apiError"] = apiResp.getError()
			} else {
				redirectAttrs.addFlashAttribute("createdId", apiResp.getElemId())
			}
		}

		if (hasErrors) {
			model["userId"] = getAuthUserId()
			model["formDataArticle"] = fdArticle
			return "ui/data/articles_create"
		}

		redirectAttrs.addFlashAttribute("createdMsg", "Created a new Article")
		return "redirect:/ui/articles/show"
	}

	// -----------------------------------------------------------------------------------------------------------------

	@GetMapping(value = ["/ui/products/show"])
	fun getUiProductsShow(model: Model): String {
		val reqUrl = "${cfgRscSrvUrl}/api/v1/products"
		val apiResp: ApiCrudResponseRead<ApiDataProduct> = getRecordArrayFromRscSrv(reqUrl)
		model["dataArr"] = apiResp.getElems()
		return "/ui/data/products_show"
	}

	@GetMapping(value = ["/ui/products/create"])
	fun getUiProductsCreate(model: Model): String {
		model["targetUrl"] = "/ui/products/create"
		model["userId"] = getAuthUserId()
		model["formDataProduct"] = FormDataProduct()
		return "/ui/data/products_create"
	}

	@PostMapping(value = ["/ui/products/create"])
	fun postUiProductsCreate(
				@Valid @ModelAttribute fdProduct: FormDataProduct,
				errors: BindingResult,
				model: Model,
				redirectAttrs: RedirectAttributes
			): String {
		var hasErrors = errors.hasErrors()

		if (! hasErrors) {
			val endpointUri = "$cfgRscSrvUrl/api/v1/products"
			val apiDataProduct = ApiDataProduct.withId("bogus")
					.userId("bogus")
					.desc(
							fdProduct.desc!!
						)
					.price(
							fdProduct.price!!
						)
					.build()
			val apiResp: ApiCrudResponseCreate = createRecordOnRscSrv(apiDataProduct, endpointUri)
			if (! apiResp.getOk()) {
				hasErrors = true
				model["apiError"] = apiResp.getError()
			} else {
				redirectAttrs.addFlashAttribute("createdId", apiResp.getElemId())
			}
		}

		if (hasErrors) {
			model["userId"] = getAuthUserId()
			model["formDataProduct"] = fdProduct
			return "ui/data/products_create"
		}

		redirectAttrs.addFlashAttribute("createdMsg", "Created a new Product")
		return "redirect:/ui/products/show"
	}

	// -----------------------------------------------------------------------------------------------------------------

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

	private fun getAuthUserId(): String {
		val authentication: Authentication = SecurityContextHolder.getContext().authentication
		if (authentication is OAuth2AuthenticationToken) {
			val authToken: OAuth2AuthenticationToken = authentication
			return authToken.name
		}
		throw IllegalStateException("cannot determine Auth User ID")
	}

	private fun getValueOrNull(value: Any?): String {
		return value?.toString() ?: "NULL"
	}

	private inline fun <reified T : ApiDataInterface> typeReference() = object : ParameterizedTypeReference<ApiCrudResponseRead<T>>() {}

	@Throws(ClientAuthorizationException::class, ResponseStatusException::class)
	private inline fun <reified T : ApiDataInterface> getRecordArrayFromRscSrv(reqUrl: String): ApiCrudResponseRead<T> {
		try {
			return webClientRscSrv
					.get()
					.uri(reqUrl)
					.attributes(
							clientRegistrationId(cfgClientWebAppInternalClientId)
						)
					.retrieve()
					.bodyToMono(
							typeReference<T>()
						)
					.block()
					?: throw ResponseStatusException(HttpStatus.NO_CONTENT, "No data received")
		} catch (err: WebClientResponseException.Unauthorized) {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"UnauthorizedException, " +
					"method=GET, " +
					"url=$reqUrl, " +
					"status=" + err.rawStatusCode + ", " +
					"body=" + err.responseBodyAsString + ", " +
					"error=" + err.message
				)
		} catch (err: ClientAuthorizationException) {
			if (err.error.errorCode == "client_authorization_required") {
				// This usually happens when the OAuth2 Access Token has expired or doesn't exist yet.
				// The Web Client will then automatically fetch a new one.
				throw err
			}
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"ClientAuthorizationException, " +
					"method=GET, " +
					"url=$reqUrl, " +
					"error=" + err.message + ", " +
					"errorCode=" + err.error.errorCode
				)
		} catch (err: WebClientResponseException.Forbidden) {
			val authentication: Authentication = SecurityContextHolder.getContext().authentication
			throw ResponseStatusException(HttpStatus.FORBIDDEN,
					"ForbiddenException, " +
					"Auth User=" + (if (authentication !is AnonymousAuthenticationToken) { (authentication.principal as DefaultOidcUser).name } else { "-" }) + ", " +
					"Auth Authorities=" + (if (authentication !is AnonymousAuthenticationToken) { authentication.authorities.toString() } else { "-" }) + ", " +
					"method=GET, " +
					"url=$reqUrl, " +
					"status=" + err.rawStatusCode + ", " +
					"body=" + err.responseBodyAsString + ", " +
					"error=" + err.message
				)
		} catch (err: WebClientResponseException) {
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"WebClientResponseException, " +
					"method=GET, " +
					"url=$reqUrl, " +
					"status=" + err.rawStatusCode + ", " +
					"body=" + err.responseBodyAsString + ", " +
					"error=" + err.message
				)
		} catch (err: WebClientException) {
			throw ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"WebClientException, " +
							"method=GET, " +
							"url=$reqUrl, " +
							"error=" + err.message
			)
		} catch (err: DecodingException) {
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"DecodingException, " +
					"error=" + err.message
				)
		} catch (err: IllegalArgumentException) {
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"IllegalArgumentException, " +
					"error=" + err.message
				)
		}
	}

	@Throws(ResponseStatusException::class)
	private fun <T : ApiDataInterface> createRecordOnRscSrv(apiData: T, endpointUri: String): ApiCrudResponseCreate {
		val resErr = ApiCrudResponseCreate.Companion.Builder()
				.ok(false)
				.error("unknown")
		try {
			return webClientRscSrv
					.post().uri(endpointUri)
					.attributes(
							clientRegistrationId(cfgClientWebAppInternalClientId)
						)
					.body(
							BodyInserters.fromValue(apiData)
						)
					.retrieve()
					.bodyToMono(ApiCrudResponseCreate::class.java)
					.block()
					?: throw ResponseStatusException(HttpStatus.NO_CONTENT, "No data received")
		} catch (err: ClientAuthorizationException) {
			if (err.error.errorCode == "client_authorization_required") {
				// This usually happens when the OAuth2 Access Token has expired or doesn't exist yet.
				// The Web Client will then automatically fetch a new one.
				throw err
			}
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"ClientAuthorizationException, " +
					"method=POST, " +
					"url=$endpointUri, " +
					"error=" + err.message + ", " +
					"errorCode=" + err.error.errorCode
				)
		} catch (err: WebClientResponseException.BadRequest) {
			resErr.error("BadRequest, URL=${endpointUri}, Message: ${err.message}")
		} catch (err: WebClientResponseException.Unauthorized) {
			resErr.error("Unauthorized, URL=$endpointUri")
		} catch (err: WebClientResponseException.Forbidden) {
			resErr.error("Forbidden, URL=$endpointUri")
		} catch (err: WebClientResponseException.NotFound) {
			resErr.error("NotFound, URL=$endpointUri")
		} catch (err: WebClientResponseException.MethodNotAllowed) {
			resErr.error("MethodNotAllowed, URL=$endpointUri")
		}
		return resErr.build()
	}
}
