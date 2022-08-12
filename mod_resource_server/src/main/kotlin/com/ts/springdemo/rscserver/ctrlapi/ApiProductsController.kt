package com.ts.springdemo.rscserver.ctrlapi

import com.ts.springdemo.common.entity.ApiDataProduct
import com.ts.springdemo.rscserver.entity.DbDataProduct
import com.ts.springdemo.rscserver.repository.DbDataProductRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiProductsController(
			@Autowired
			private val dbDataProductRepository: DbDataProductRepository
		) {

	@GetMapping("/api/v1/products")
	fun getProducts(): List<ApiDataProduct> {
		val authentication = SecurityContextHolder.getContext().authentication
		val dbProducts: List<DbDataProduct> = if (! authentication?.name.isNullOrEmpty()) {
				dbDataProductRepository.findByUserId(authentication.name)
			} else {
				emptyList()
			}
		val res: MutableList<ApiDataProduct> = mutableListOf()
		dbProducts.forEach {
				res.add(it.toApiDataProduct())
			}
		return res
	}
}
