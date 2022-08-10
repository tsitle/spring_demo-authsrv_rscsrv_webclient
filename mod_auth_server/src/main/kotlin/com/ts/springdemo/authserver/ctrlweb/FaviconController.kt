package com.ts.springdemo.authserver.ctrlweb

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@RestController
class FaviconController {
	@RequestMapping("/favicon.ico")
	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="There's no favicon yet")
	fun favicon(): String {
		return ""
	}
}
