package com.ts.springdemo.common.entityapi

import org.springframework.util.Assert
import java.io.Serializable


@Suppress("unused")
data class ApiCrudResponseRead<T : ApiDataInterface>(
			private var ok: Boolean = false,
			private var error: String = "",
			private var elems: Set<T> = emptySet()
		) : ApiCrudResponseInterface {

	fun getOk(): Boolean {
		return ok
	}

	fun getError(): String {
		return error
	}

	fun getElems(): Set<T> {
		return elems
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) {
			return true
		}
		if (javaClass != other?.javaClass) {
			return false
		}

		@Suppress("UNCHECKED_CAST")
		other as ApiCrudResponseRead<T>

		if (ok != other.ok) {
			return false
		}
		if (error != other.error) {
			return false
		}
		if (elems != other.elems) {
			return false
		}
		return true
	}

	override fun hashCode(): Int {
		var result = ok.hashCode()
		result = 31 * result + error.hashCode()
		result = 31 * result + elems.hashCode()
		return result
	}


	companion object {
		class Builder<T : ApiDataInterface> : Serializable {
			companion object {
				private const val serialVersionUID = "0.0.1"
			}
			private var ok: Boolean? = null
			private var error: String? = null
			private var elems: Set<T>? = null

			fun ok(ok: Boolean): Builder<T> {
				this.ok = ok
				return this
			}

			fun error(error: String): Builder<T> {
				this.error = error
				return this
			}

			fun elems(elems: Set<T>): Builder<T> {
				this.elems = elems
				return this
			}

			fun build(): ApiCrudResponseRead<T> {
				Assert.notNull(ok, "ok cannot be null")
				if (ok == false) {
					Assert.hasText(error, "error cannot be empty if ok==false")
				}
				return create()
			}

			private fun create(): ApiCrudResponseRead<T> {
				val apiResp = ApiCrudResponseRead<T>()
				apiResp.ok = ok!!
				apiResp.error = error ?: ""
				apiResp.elems = elems ?: emptySet()
				return apiResp
			}
		}
	}
}
