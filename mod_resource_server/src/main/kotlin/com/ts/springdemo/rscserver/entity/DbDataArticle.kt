package com.ts.springdemo.rscserver.entity

import org.springframework.data.mongodb.core.mapping.Document
import com.ts.springdemo.common.entityapi.ApiDataArticle
import org.springframework.util.Assert
import java.io.Serializable
import java.util.*


@Suppress("unused")
@Document(collection = "dataArticle")
data class DbDataArticle(
			private var id: String = "",
			private var userId: String = "",
			private var lines: List<String> = mutableListOf()
		) : Serializable {

	fun getId(): String {
		return id
	}

	fun getUserId(): String {
		return userId
	}

	fun getLines(): List<String> {
		return lines
	}

	fun toApiDataArticle(): ApiDataArticle {
		return ApiDataArticle.withId(this.id)
				.userId(userId)
				.lines(lines)
				.build()
	}


	companion object {
		private const val serialVersionUID = "0.0.1"

		fun withId(id: String): Builder {
			Assert.hasText(id, "id cannot be empty")
			return Builder(id)
		}

		fun withRandomId(): Builder {
			return Builder(UUID.randomUUID().toString())
		}

		fun from(dbDataArticle: DbDataArticle): Builder {
			return Builder(dbDataArticle)
		}

		fun from(apiDataArticle: ApiDataArticle): Builder {
			return Builder(apiDataArticle.getId())
					.userId(apiDataArticle.getUserId())
					.lines(apiDataArticle.getLines())
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

			constructor(dbDataArticle: DbDataArticle) {
				id = dbDataArticle.id
				userId = dbDataArticle.userId
				lines = dbDataArticle.lines
			}

			fun userId(userId: String): Builder {
				this.userId = userId
				return this
			}

			fun lines(lines: List<String>): Builder {
				this.lines = lines
				return this
			}

			fun build(): DbDataArticle {
				Assert.hasText(id, "id cannot be empty")
				Assert.hasText(userId, "userId cannot be empty")
				Assert.notNull(lines, "lines cannot be null")
				Assert.state(lines?.isNotEmpty() ?: false, "lines cannot be empty")
				return create()
			}

			private fun create(): DbDataArticle {
				val dbDataArticle = DbDataArticle()
				dbDataArticle.id = id!!
				dbDataArticle.userId = userId!!
				dbDataArticle.lines = lines!!
				return dbDataArticle
			}
		}
	}
}
