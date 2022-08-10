package com.ts.springdemo.authserver.config

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MappingMongoConverter


@Configuration
class MongoConfig {
	@Value("\${spring.datasource.url}")
	private val cfgMongoDataSourceVal: String = ""

	@Value("\${spring.data.mongodb.database}")
	private val cfgMongoDatabaseVal: String = ""

	@Bean
	@Throws(IllegalArgumentException::class)
	fun mongoClient(): MongoClient {
		if (cfgMongoDataSourceVal.startsWith("jdbc:")) {
			throw IllegalArgumentException("MongoDB URI may not start with 'jdbc:'")
		}
		return MongoClients.create(
				ConnectionString(cfgMongoDataSourceVal)
			)
	}

	@Bean
	fun mongoTemplate(): MongoTemplate {
		val templ = MongoTemplate(mongoClient(), cfgMongoDatabaseVal)

		if (templ.converter is MappingMongoConverter) {
			(templ.converter as MappingMongoConverter).setMapKeyDotReplacement("#DOT#")
		}
		return templ
	}
}
