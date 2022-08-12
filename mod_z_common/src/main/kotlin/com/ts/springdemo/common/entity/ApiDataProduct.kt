package com.ts.springdemo.common.entity

import org.springframework.util.Assert
import java.io.Serializable


@Suppress("unused")
data class ApiDataProduct(
			private var id: String = "",
			private var userId: String = "",
			private var desc: String = "",
			private var price: Double = -1.0
		) : Serializable {

	fun getId(): String {
		return id
	}

	fun getUserId(): String {
		return userId
	}

	fun getDesc(): String {
		return desc
	}

	fun getPrice(): Double {
		return price
	}


	companion object {
		private const val serialVersionUID = "0.0.1"

		fun withId(id: String): Builder {
			Assert.hasText(id, "id cannot be empty")
			return Builder(id)
		}

		fun from(apiDataProduct: ApiDataProduct): Builder {
			return Builder(apiDataProduct)
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

			constructor(apiDataProduct: ApiDataProduct) {
				id = apiDataProduct.id
				userId = apiDataProduct.userId
				desc = apiDataProduct.desc
				price = apiDataProduct.price
			}

			fun userId(userId: String): Builder {
				this.userId = userId
				return this
			}

			fun desc(desc: String): Builder {
				this.desc = desc
				return this
			}

			fun price(price: Double): Builder {
				this.price = price
				return this
			}

			fun build(): ApiDataProduct {
				Assert.hasText(id, "id cannot be empty")
				Assert.hasText(userId, "userId cannot be empty")
				Assert.notNull(desc, "desc cannot be null")
				Assert.notNull(price, "price cannot be null")
				return create()
			}

			private fun create(): ApiDataProduct {
				val apiDataProduct = ApiDataProduct()
				apiDataProduct.id = id!!
				apiDataProduct.userId = userId!!
				apiDataProduct.desc = desc!!
				apiDataProduct.price = price!!
				return apiDataProduct
			}
		}
	}
}
