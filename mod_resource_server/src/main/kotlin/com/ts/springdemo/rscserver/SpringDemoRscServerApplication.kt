package com.ts.springdemo.rscserver

import com.ts.springdemo.rscserver.entity.DbDataArticle
import com.ts.springdemo.rscserver.entity.DbDataProduct
import com.ts.springdemo.rscserver.repository.DbDataArticleRepository
import com.ts.springdemo.rscserver.repository.DbDataProductRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.*


@SpringBootApplication
@EnableConfigurationProperties(CustomAppProperties::class)
class SpringDemoRscServerApplication(
			@Autowired
			private val customAppProperties: CustomAppProperties,
			@Autowired
			private val dbDataArticleRepository: DbDataArticleRepository,
			@Autowired
			private val dbDataProductRepository: DbDataProductRepository
		) {

	@Bean
	fun init(): CommandLineRunner? {
		return CommandLineRunner {
				// bogus operation for initializing the DB connection
				dbDataArticleRepository.findByUserId("does.not@exist")
				//
				println("----------------------------------------")
				println("----------------------------------------")
				println("----------------------------------------")
				println("----------------------------------------")
				if (customAppProperties.resourceServer.db.pruneCollections) {
					pruneCollections()
					println("----------------------------------------")
				}
				if (customAppProperties.resourceServer.db.initCollections) {
					initArticlesRepository()
					println("----------------------------------------")
					initProductsRepository()
					println("----------------------------------------")
				}
				val piUrl = customAppProperties.authServer.providerIssuerUrl
				println("Provider Issuer URL='$piUrl' (important for JWT)")
				println("----------------------------------------")
				println("----------------------------------------")
				println("----------------------------------------")
				println("----------------------------------------")
			}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun pruneCollections() {
		println("Prune Collections")
		println("  - dataArticle")
		dbDataArticleRepository.deleteAll()
		println("  - dataProduct")
		dbDataProductRepository.deleteAll()
	}

	private fun initArticlesRepository() {
		customAppProperties.resourceServer.dataArticlesAp.forEach { (itId: String, itEntry: CustomAppProperties.ResourceServer.DataArticle) ->
				val dbExistingMap: Optional<DbDataArticle?> = dbDataArticleRepository.findById(itId)
				//
				println("ArticlesRepository: ID='$itId'")
				//
				if (dbExistingMap.isPresent) {
					println("  --> already existed. skipping.")
				} else {
					val dbEntry = DbDataArticle.withId(itId)
							.userId(itEntry.userId)
							.lines(itEntry.lines)
							.build()
					dbDataArticleRepository.save(dbEntry)
				}
			}
	}

	private fun initProductsRepository() {
		customAppProperties.resourceServer.dataProductsAp.forEach { (itId: String, itEntry: CustomAppProperties.ResourceServer.DataProduct) ->
				val dbExistingMap: Optional<DbDataProduct?> = dbDataProductRepository.findById(itId)
				//
				println("ProductRepository: ID='$itId'")
				//
				if (dbExistingMap.isPresent) {
					println("  --> already existed. skipping.")
				} else {
					val dbEntry = DbDataProduct.withId(itId)
							.userId(itEntry.userId)
							.desc(itEntry.desc)
							.price(itEntry.price)
							.build()
					dbDataProductRepository.save(dbEntry)
				}
			}
	}
}

// -----------------------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------

fun main(args: Array<String>) {
	runApplication<SpringDemoRscServerApplication>(*args)
}
