package com.ts.springdemo.authserver.repository

import com.ts.springdemo.authserver.entity.CustomOAuth2Authorization
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface CustomOAuth2AuthorizationRepository : MongoRepository<CustomOAuth2Authorization?, String?> {
	@Query(value = "{ ?0 : ?1 }" )
	fun findByTokenTypeAndTokenValue(tokenKey: String, tokenValue: String): List<CustomOAuth2Authorization?>?

	fun findOneById(id: String): CustomOAuth2Authorization?
}
