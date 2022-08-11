package com.ts.springdemo.oauthwebclient.ctrlweb

import com.ts.springdemo.common.constants.AuthScope
import com.ts.springdemo.common.constants.AuthSrvOauth
import com.ts.springdemo.oauthwebclient.repository.CustomClientRegistrationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletResponse


@RestController
class WebAuthRequController(
			@Autowired
			private val customClientRegistrationRepository: CustomClientRegistrationRepository,
			@Value("\${custom-app.auth-server.provider-issuer-url}")
			private val cfgAuthSrvUrl: String,
			@Value("\${custom-app.client-web-app.url}")
			private val cfgClientWebAppUrl: String,
			@Value("\${custom-app.client-web-app.internal-client-id}")
			private val cfgClientWebAppInternalClientId: String
		) {

	@GetMapping(value = ["/custom/authcode/fetch"])
	@Throws(ResponseStatusException::class)
	fun getAuthCustomAuthcodeFetch(response: HttpServletResponse) {
		val oauth2ClientReg: ClientRegistration = customClientRegistrationRepository.findByRegistrationId(cfgClientWebAppInternalClientId)
				?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth2 Client '$cfgClientWebAppInternalClientId' not found")

		val redirUrl = cfgAuthSrvUrl + AuthSrvOauth.URL_PATH_AUTHORIZATION + "?" +
				"response_type=code&" +
				"client_id=${oauth2ClientReg.clientId}&" +
				"redirect_uri=$cfgClientWebAppUrl/custom/authcode/authorized&" +
				"scope=" + AuthScope.EnScopes.OPENID.value
		response.setHeader("Location", redirUrl)
		response.status = 302
	}

	@GetMapping(value = ["/logout"])
	fun getAuthLogout(response: HttpServletResponse) {
		val redirUrl = "${cfgAuthSrvUrl}/logout?" + AuthSrvOauth.URL_PARAM_LOGOUT_PAGE_REDIRECT_TO_WEBCLIENT + "=1"
		response.setHeader("Location", redirUrl)
		response.status = 302
	}
}
