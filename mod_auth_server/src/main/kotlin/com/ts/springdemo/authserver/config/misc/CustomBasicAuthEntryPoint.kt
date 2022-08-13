package com.ts.springdemo.authserver.config.misc

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.PrintWriter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class CustomBasicAuthEntryPoint : BasicAuthenticationEntryPoint() {
	override fun commence(request: HttpServletRequest, response: HttpServletResponse, authEx: AuthenticationException) {
		response.addHeader("WWW-Authenticate", "Basic realm='" + this.realmName + "'")
		response.status = HttpServletResponse.SC_UNAUTHORIZED
		val writer: PrintWriter = response.writer
		writer.println("HTTP Status 401 - " + authEx.message)
	}

	override fun afterPropertiesSet() {
		this.realmName = "SpringRealm"
		super.afterPropertiesSet()
	}
}
