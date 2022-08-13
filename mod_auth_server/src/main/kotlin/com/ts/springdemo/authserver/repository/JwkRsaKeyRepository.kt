package com.ts.springdemo.authserver.repository

import com.ts.springdemo.authserver.entity.DbJwkRsaKey
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface JwkRsaKeyRepository : MongoRepository<DbJwkRsaKey?, String?>
