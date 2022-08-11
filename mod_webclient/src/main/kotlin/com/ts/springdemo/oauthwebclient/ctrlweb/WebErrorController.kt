package com.ts.springdemo.oauthwebclient.ctrlweb

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest


@Controller
class WebErrorController : ErrorController {
	@RequestMapping("/error")
	fun handleError(request: HttpServletRequest, model: Model): String? {
		val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
		model["statusDesc"] = "Unknown"
		model["statusCode"] = -1
		model["linkDesc"] = "Home"
		model["linkUrl"] = "/"
		if (status != null) {
			val statusInt = Integer.valueOf(status.toString())
			model["statusCode"] = statusInt
			model["statusDesc"] = when (statusInt) {
				HttpStatus.BAD_REQUEST.value() -> "BAD REQUEST"
				HttpStatus.UNAUTHORIZED.value() -> "UNAUTHORIZED"
				HttpStatus.FORBIDDEN.value() -> "FORBIDDEN"
				HttpStatus.NOT_FOUND.value() -> "NOT FOUND"
				HttpStatus.INTERNAL_SERVER_ERROR.value() -> "INTERNAL SERVER ERROR"
				HttpStatus.NO_CONTENT.value() -> "NO CONTENT"
				else -> "Unknown"
			}
			if (statusInt == HttpStatus.UNAUTHORIZED.value()) {
				model["linkDesc"] = "Login"
				model["linkUrl"] = "/login"
			}
		}
		return "errors/error-generic"
	}
}
