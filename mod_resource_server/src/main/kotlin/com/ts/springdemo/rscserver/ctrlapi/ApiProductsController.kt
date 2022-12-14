package com.ts.springdemo.rscserver.ctrlapi

import com.ts.springdemo.common.entityapi.ApiDataProduct
import com.ts.springdemo.common.entityapi.ApiCrudResponseCreate
import com.ts.springdemo.common.entityapi.ApiCrudResponseRead
import com.ts.springdemo.rscserver.entity.DbDataProduct
import com.ts.springdemo.rscserver.repository.DbDataProductRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
class ApiProductsController(
			@Autowired
			private val dbDataProductRepository: DbDataProductRepository
		) {

	@Throws(ResponseStatusException::class)
	@GetMapping("/api/v1/products")
	fun getProducts(): ApiCrudResponseRead<ApiDataProduct> {
		val authentication = SecurityContextHolder.getContext().authentication
				?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "OAuth2 Login required")
		val userEmail: String = authentication.name ?: ""
		//
		if (userEmail.isEmpty() || ! userEmail.contains("@")) {
			return ApiCrudResponseRead.Companion.Builder<ApiDataProduct>()
					.ok(false)
					.error("no valid authentication found")
					.build()
		}
		//
		val dbProducts: List<DbDataProduct> = dbDataProductRepository.findByUserId(userEmail)
		val listApiData: MutableSet<ApiDataProduct> = mutableSetOf()
		dbProducts.forEach {
				listApiData.add(it.toApiDataProduct())
			}
		return ApiCrudResponseRead.Companion.Builder<ApiDataProduct>()
				.ok(true)
				.elems(listApiData)
				.build()
	}

	@PostMapping("/api/v1/products")
	fun postArticles(@RequestBody apiDataArticle: ApiDataProduct): ApiCrudResponseCreate {
		val authUserName = SecurityContextHolder.getContext().authentication.name
		val dbProduct = DbDataProduct.withRandomId()
				.userId(authUserName)
				.desc(apiDataArticle.getDesc())
				.price(apiDataArticle.getPrice())
				.build()
		dbDataProductRepository.save(dbProduct)
		//
		return ApiCrudResponseCreate.Companion.Builder()
				.ok(true)
				.elemId(
						dbProduct.getId()
					)
				.build()
	}
}
