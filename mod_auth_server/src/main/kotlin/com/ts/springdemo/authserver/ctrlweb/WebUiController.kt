package com.ts.springdemo.authserver.ctrlweb

import com.ts.springdemo.authserver.ctrlapi.ApiCustomUserinfoController
import com.ts.springdemo.authserver.entity.CustomAuthUser
import com.ts.springdemo.authserver.entity.oidc.CustomOidcUserInfo
import com.ts.springdemo.authserver.repository.oidc.CustomOidcUserInfoRepository
import com.ts.springdemo.authserver.service.CustomAuthUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.server.ResponseStatusException


@Controller
class WebUiController(
			@Autowired
			private val customOidcUserInfoRepository: CustomOidcUserInfoRepository,
			@Autowired
			private val customAuthUserService: CustomAuthUserService
		) {

	@GetMapping("/")
	fun getUiRoot(authentication: UsernamePasswordAuthenticationToken, model: Model): String {
		if (authentication.principal == null) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Authentication")
		}
		val authUser: CustomAuthUser = customAuthUserService.findByUserEmail(authentication.principal as String?)
				?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "AuthUser not found")
		val oidcUserInfo: CustomOidcUserInfo? = customOidcUserInfoRepository.findByAuthUserId(authUser.getId())
		val outUsername = oidcUserInfo?.getName() ?: authUser.getEmail()

		model["username"] = outUsername
		return "ui/root"
	}

	@GetMapping("/ui/userinfo/in_browser")
	fun getUserinfoInBrowser(authentication: UsernamePasswordAuthenticationToken, model: Model): String {
		if (authentication.principal == null) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Authentication")
		}
		val apiUserInfo: Map<String, Any> = ApiCustomUserinfoController.getUserInfo(
				authentication.principal as String?,
				customAuthUserService
			)
		model["userinfo"] = apiUserInfo
		return "ui/userinfo/in_browser"
	}
}
