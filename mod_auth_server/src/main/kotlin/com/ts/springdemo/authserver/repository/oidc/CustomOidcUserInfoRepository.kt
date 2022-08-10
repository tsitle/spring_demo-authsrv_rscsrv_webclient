package com.ts.springdemo.authserver.repository.oidc

import com.ts.springdemo.authserver.entity.oidc.CustomOidcUserInfo
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface CustomOidcUserInfoRepository : MongoRepository<CustomOidcUserInfo?, String?> {
	fun findByAuthUserId(authUserId: String?): CustomOidcUserInfo?
}
