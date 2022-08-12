package com.ts.springdemo.rscserver.entity

import org.springframework.data.mongodb.core.mapping.Document
import com.ts.springdemo.common.entity.ApiDataProduct
import org.springframework.util.Assert
import java.io.Serializable


@Suppress("unused")
@Document(collection = "dataProduct")
data class DbDataProduct(
			private var id: String = "",
			private var userId: String = "",
			private var desc: String? = null,
			private var price: Double? = null
		) : Serializable {

	fun getId(): String {
		return id
	}

	fun getUserId(): String {
		return userId
	}

	fun getDesc(): String? {
		return desc
	}

	fun getPrice(): Double? {
		return price
	}

	fun toApiDataProduct(): ApiDataProduct {
		return ApiDataProduct.withId(this.id)
				.userId(userId)
				.desc(desc ?: "")
				.price(price ?: -1.0)
				.build()
	}


	companion object {
		private const val serialVersionUID = "0.0.1"

		fun withId(id: String): Builder {
			Assert.hasText(id, "id cannot be empty")
			return Builder(id)
		}

		fun from(dbDataProduct: DbDataProduct): Builder {
			return Builder(dbDataProduct)
		}

		fun from(apiDataProduct: ApiDataProduct): Builder {
			return Builder(apiDataProduct.getId())
					.userId(apiDataProduct.getUserId())
					.desc(apiDataProduct.getDesc())
					.price(apiDataProduct.getPrice())
		}


		class Builder : Serializable {
			companion object {
				private const val serialVersionUID = "0.0.1"
			}
			private var id: String? = null
			private var userId: String? = null
			private var desc: String? = null
			private var price: Double? = null

			constructor(id: String) {
				this.id = id
			}

			constructor(dbDataProduct: DbDataProduct) {
				id = dbDataProduct.id
				userId = dbDataProduct.userId
				desc = dbDataProduct.desc
				price = dbDataProduct.price
			}

			fun userId(userId: String): Builder {
				this.userId = userId
				return this
			}

			fun desc(desc: String?): Builder {
				this.desc = desc
				return this
			}

			fun price(price: Double?): Builder {
				this.price = price
				return this
			}

			fun build(): DbDataProduct {
				Assert.hasText(id, "id cannot be empty")
				Assert.hasText(userId, "userId cannot be empty")
				return create()
			}

			private fun create(): DbDataProduct {
				val dbDataProduct = DbDataProduct()
				dbDataProduct.id = id!!
				dbDataProduct.userId = userId!!
				dbDataProduct.desc = desc
				dbDataProduct.price = price
				return dbDataProduct
			}
		}
	}
}
