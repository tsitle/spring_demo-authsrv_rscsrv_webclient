package com.ts.springdemo.common.entityapi

import org.springframework.util.Assert
import java.io.Serializable


@Suppress("unused")
data class ApiDataArticle(
			private var id: String = "",
			private var userId: String = "",
			private var lines: List<String> = mutableListOf()
		) : ApiDataInterface {

	fun getId(): String {
		return id
	}

	fun getUserId(): String {
		return userId
	}

	fun getLines(): List<String> {
		return lines
	}


	companion object {
		fun withId(id: String): Builder {
			Assert.hasText(id, "id cannot be empty")
			return Builder(id)
		}

		fun from(apiDataArticle: ApiDataArticle): Builder {
			return Builder(apiDataArticle)
		}

		class Builder : Serializable {
			companion object {
				private const val serialVersionUID = "0.0.1"
			}
			private var id: String? = null
			private var userId: String? = null
			private var lines: List<String>? = null

			constructor(id: String) {
				this.id = id
			}

			constructor(apiDataArticle: ApiDataArticle) {
				id = apiDataArticle.id
				userId = apiDataArticle.userId
				lines = apiDataArticle.lines
			}

			fun userId(userId: String): Builder {
				this.userId = userId
				return this
			}

			fun lines(lines: List<String>): Builder {
				this.lines = lines
				return this
			}

			fun build(): ApiDataArticle {
				Assert.hasText(id, "id cannot be empty")
				Assert.hasText(userId, "userId cannot be empty")
				Assert.notNull(lines, "lines cannot be null")
				Assert.state(lines?.isNotEmpty() ?: false, "lines cannot be empty")
				return create()
			}

			private fun create(): ApiDataArticle {
				val apiDataArticle = ApiDataArticle()
				apiDataArticle.id = id!!
				apiDataArticle.userId = userId!!
				apiDataArticle.lines = lines!!
				return apiDataArticle
			}
		}
	}
}
