package com.ts.springdemo.rscserver.ctrlapi

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiProductsController {
	@GetMapping("/api/v1/products")
	fun getProducts(): Array<String> {
		val authentication = SecurityContextHolder.getContext().authentication

		return arrayOf("User=" + authentication.name, "Product 1", "Product 2", "Product 3")
	}
}
