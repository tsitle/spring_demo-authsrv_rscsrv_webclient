package com.ts.springdemo.authserver.ctrlweb

import com.ts.springdemo.authserver.entity.CustomAuthUser
import com.ts.springdemo.authserver.entity.oidc.CustomOidcUserInfo
import com.ts.springdemo.authserver.repository.CustomAuthUserRepository
import com.ts.springdemo.authserver.repository.oidc.CustomOidcUserInfoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
class WebUiController(
			@Autowired
			private val customAuthUserRepository: CustomAuthUserRepository,
			@Autowired
			private val customOidcUserInfoRepository: CustomOidcUserInfoRepository
		) {

	@GetMapping("/")
	fun getUiRoot(authentication: UsernamePasswordAuthenticationToken): String {
		if (authentication.principal == null) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Authentication")
		}
		val authUser: CustomAuthUser = customAuthUserRepository.findByEmail(authentication.principal as String?) ?:
				throw ResponseStatusException(HttpStatus.BAD_REQUEST, "AuthUser not found")
		val oidcUserInfo: CustomOidcUserInfo? = customOidcUserInfoRepository.findByAuthUserId(authUser.getId())
		val outUsername = oidcUserInfo?.getName() ?: authUser.getEmail()
		return "Hello $outUsername"
	}

	@GetMapping("/error/forbidden")
	fun getUiErrorForbidden(): String {
		return "Forbidden"
	}
}
