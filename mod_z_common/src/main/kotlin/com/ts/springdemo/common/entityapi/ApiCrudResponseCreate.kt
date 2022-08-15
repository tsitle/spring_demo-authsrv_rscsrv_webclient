package com.ts.springdemo.common.entityapi

import org.springframework.util.Assert
import java.io.Serializable


@Suppress("unused")
data class ApiCrudResponseCreate(
			private var ok: Boolean = false,
			private var error: String = "",
			private var elemId: String = "",
		) : ApiCrudResponseInterface {

	fun getOk(): Boolean {
		return ok
	}

	fun getError(): String {
		return error
	}

	fun getElemId(): String {
		return elemId
	}


	companion object {
		class Builder : Serializable {
			companion object {
				private const val serialVersionUID = "0.0.1"
			}
			private var ok: Boolean? = null
			private var error: String? = null
			private var elemId: String? = null

			fun ok(ok: Boolean): Builder {
				this.ok = ok
				return this
			}

			fun error(error: String): Builder {
				this.error = error
				return this
			}

			fun elemId(elemId: String): Builder {
				this.elemId = elemId
				return this
			}

			fun build(): ApiCrudResponseCreate {
				Assert.notNull(ok, "ok cannot be null")
				if (ok == true) {
					Assert.hasText(elemId, "elemId cannot be empty if 'ok'==true")
				}
				return create()
			}

			private fun create(): ApiCrudResponseCreate {
				val apiDataArticle = ApiCrudResponseCreate()
				apiDataArticle.ok = ok!!
				apiDataArticle.error = error ?: ""
				apiDataArticle.elemId = elemId ?: ""
				return apiDataArticle
			}
		}
	}
}
