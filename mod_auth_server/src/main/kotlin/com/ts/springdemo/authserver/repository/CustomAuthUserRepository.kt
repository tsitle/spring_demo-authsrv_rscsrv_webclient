package com.ts.springdemo.authserver.repository

import com.ts.springdemo.authserver.entity.CustomAuthUser
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface CustomAuthUserRepository : MongoRepository<CustomAuthUser?, String?> {
	fun findByEmail(email: String?): CustomAuthUser?
}
