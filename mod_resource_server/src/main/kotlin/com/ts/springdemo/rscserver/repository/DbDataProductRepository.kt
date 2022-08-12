package com.ts.springdemo.rscserver.repository

import com.ts.springdemo.rscserver.entity.DbDataProduct
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface DbDataProductRepository : MongoRepository<DbDataProduct?, String?> {
	fun findByUserId(userEmail: String): List<DbDataProduct>
}
