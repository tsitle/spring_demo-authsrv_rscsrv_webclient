package com.ts.springdemo.rscserver.ctrlapi

import com.ts.springdemo.common.entity.ApiDataArticle
import com.ts.springdemo.rscserver.entity.DbDataArticle
import com.ts.springdemo.rscserver.repository.DbDataArticleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiArticlesController(
			@Autowired
			private val dbDataArticleRepository: DbDataArticleRepository
		) {

	@GetMapping("/api/v1/articles")
	fun getArticles(): Array<ApiDataArticle> {
		val authentication = SecurityContextHolder.getContext().authentication
		val dbArticles: List<DbDataArticle> = if (! authentication?.name.isNullOrEmpty()) {
				dbDataArticleRepository.findByUserId(authentication.name)
			} else {
				emptyList()
			}
		val res: MutableList<ApiDataArticle> = mutableListOf()
		dbArticles.forEach {
				res.add(it.toApiDataArticle())
			}
		return res.toTypedArray()
	}
}
