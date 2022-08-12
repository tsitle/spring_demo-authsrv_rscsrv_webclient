package com.ts.springdemo.authserver.ctrlweb

import org.springframework.beans.factory.annotation.Value
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

	@Value("\${custom-app.auth-server.enable-error-messages-in-web-error-controller}")
	private val cfgEnableErrMsgs: Boolean = false

	@RequestMapping("/error")
	fun handleError(request: HttpServletRequest, model: Model): String? {
		val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
		model["statusDesc"] = "Unknown"
		model["statusCode"] = -1
		model["errorMsg"] = ""
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
				else -> "Unknown"
			}
			if (statusInt == HttpStatus.UNAUTHORIZED.value()) {
				model["linkDesc"] = "Login"
				model["linkUrl"] = "/login"
			}
			if (cfgEnableErrMsgs) {
				val err = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) as Exception?
				if (err != null) {
					model["errorMsg"] = err.message ?: ""
				}
				if (model.getAttribute("errorMsg") == "") {
					val errMsg = request.getAttribute(RequestDispatcher.ERROR_MESSAGE) as String?
					if (errMsg != null) {
						model["errorMsg"] = errMsg
					}
				}
			}
		}
		return "errors/error-generic"
	}
}
