package com.ts.springdemo.rscserver.ctrlapi

import com.ts.springdemo.common.constants.AuthScope
import com.ts.springdemo.common.entityapi.ApiCrudRequestReadArticle
import com.ts.springdemo.common.entityapi.ApiDataArticle
import com.ts.springdemo.common.entityapi.ApiCrudResponseCreate
import com.ts.springdemo.common.entityapi.ApiCrudResponseRead
import com.ts.springdemo.rscserver.entity.DbDataArticle
import com.ts.springdemo.rscserver.repository.DbDataArticleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
class ApiArticlesController(
			@Autowired
			private val dbDataArticleRepository: DbDataArticleRepository
		) {

	@Throws(ResponseStatusException::class)
	@GetMapping("/api/v1/articles")
	fun getArticles(@RequestBody queryParamsJson: ApiCrudRequestReadArticle?): ApiCrudResponseRead<ApiDataArticle> {
		val authentication = SecurityContextHolder.getContext().authentication
				?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "OAuth2 Login required")
		val authenticatedUserName = authentication.name ?: ""
		val isAllowedToReadOtherUsersData = authentication.authorities.contains(
				AuthScope.buildGrantedAuthority(AuthScope.EnScopes.ACCESS_READ_OTHER_USERS_DATA)
			)
		val userEmail: String? = if (queryParamsJson != null && queryParamsJson.userEmail.isNotEmpty()) {
				queryParamsJson.userEmail
			} else {
				authenticatedUserName.ifEmpty { null }
			}
		//
		if (userEmail.isNullOrEmpty() || ! userEmail.contains("@")) {
			return ApiCrudResponseRead.Companion.Builder<ApiDataArticle>()
					.ok(false)
					.error("no valid authentication found")
					.build()
		}
		if (! isAllowedToReadOtherUsersData && userEmail != authenticatedUserName) {
			// we could also throw Error 403 (FORBIDDEN) here
			return ApiCrudResponseRead.Companion.Builder<ApiDataArticle>()
					.ok(false)
					.error("not allowed to read other user's data")
					.build()
		}
		//
		val dbArticles: List<DbDataArticle> = dbDataArticleRepository.findByUserId(userEmail)
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
