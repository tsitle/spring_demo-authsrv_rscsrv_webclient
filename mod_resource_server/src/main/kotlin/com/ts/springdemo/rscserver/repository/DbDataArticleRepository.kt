package com.ts.springdemo.rscserver.repository

import com.ts.springdemo.rscserver.entity.DbDataArticle
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface DbDataArticleRepository : MongoRepository<DbDataArticle?, String?> {
	fun findByUserId(userEmail: String): List<DbDataArticle>
}
