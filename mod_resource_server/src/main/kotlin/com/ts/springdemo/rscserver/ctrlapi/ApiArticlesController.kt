package com.ts.springdemo.rscserver.ctrlapi

import com.ts.springdemo.common.entityapi.ApiDataArticle
import com.ts.springdemo.common.entityapi.ApiCrudResponseCreate
import com.ts.springdemo.common.entityapi.ApiCrudResponseRead
import com.ts.springdemo.rscserver.entity.DbDataArticle
import com.ts.springdemo.rscserver.repository.DbDataArticleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiArticlesController(
			@Autowired
			private val dbDataArticleRepository: DbDataArticleRepository
		) {

	@GetMapping("/api/v1/articles")
	fun getArticles(): ApiCrudResponseRead<ApiDataArticle> {
		val authentication = SecurityContextHolder.getContext().authentication
		val dbArticles: List<DbDataArticle> = if (! authentication?.name.isNullOrEmpty()) {
				dbDataArticleRepository.findByUserId(authentication.name)
			} else {
				return ApiCrudResponseRead.Companion.Builder<ApiDataArticle>()
						.ok(false)
						.error("no authentication found")
						.build()
			}
		val listApiData: MutableSet<ApiDataArticle> = mutableSetOf()
		dbArticles.forEach {
				listApiData.add(it.toApiDataArticle())
			}
		return ApiCrudResponseRead.Companion.Builder<ApiDataArticle>()
				.ok(true)
				.elems(listApiData)
				.build()
	}

	@PostMapping("/api/v1/articles")
	fun postArticles(@RequestBody apiDataArticle: ApiDataArticle): ApiCrudResponseCreate {
		val authUserName = SecurityContextHolder.getContext().authentication.name
		val dbArticle = DbDataArticle.withRandomId()
				.userId(authUserName)
				.lines(apiDataArticle.getLines())
				.build()
		dbDataArticleRepository.save(dbArticle)
		//
		return ApiCrudResponseCreate.Companion.Builder()
				.ok(true)
				.elemId(
						dbArticle.getId()
					)
				.build()
	}
}
