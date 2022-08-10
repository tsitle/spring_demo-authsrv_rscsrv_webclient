package com.ts.springdemo.authserver.repository

import com.ts.springdemo.authserver.entity.RscIdPaths
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface RscIdPathsRepository : MongoRepository<RscIdPaths?, String?>
