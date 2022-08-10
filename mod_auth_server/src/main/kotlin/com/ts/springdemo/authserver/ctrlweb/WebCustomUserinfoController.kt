package com.ts.springdemo.authserver.ctrlweb

import com.ts.springdemo.authserver.ctrlapi.ApiCustomUserinfoController
import com.ts.springdemo.authserver.service.CustomUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
class WebCustomUserinfoController(
			@Autowired
			private val customUserDetailsService: CustomUserDetailsService,
		) {

	@GetMapping("/ui/userinfo/in_browser")
	fun getUserinfoInBrowser(authentication: UsernamePasswordAuthenticationToken): Map<String, String> {
		if (authentication.principal == null) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Authentication")
		}
		val userDetails: UserDetails = getAuthUserDetails(authentication.principal as String?)
		return ApiCustomUserinfoController.getUserInfo(userDetails)
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@Throws(ResponseStatusException::class)
	private fun getAuthUserDetails(userEmail: String?): UserDetails {
		if (userEmail.isNullOrEmpty()) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Username")
		}
		return customUserDetailsService.loadUserByUsername(userEmail)
	}
}
