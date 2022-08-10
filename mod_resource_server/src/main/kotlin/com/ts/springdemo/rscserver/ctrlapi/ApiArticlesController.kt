package com.ts.springdemo.rscserver.ctrlapi

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiArticlesController {
	@GetMapping("/api/v1/articles")
	fun getArticles(): Array<String> {
		val authentication = SecurityContextHolder.getContext().authentication

		return arrayOf("User=" + authentication.name, "Article 1", "Article 2", "Article 3")
	}
}
