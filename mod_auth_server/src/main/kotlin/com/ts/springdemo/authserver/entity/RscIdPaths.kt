package com.ts.springdemo.authserver.entity

import com.ts.springdemo.common.constants.AuthRscAcc
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.util.Assert
import java.io.Serializable


@Suppress("unused")
@Document(collection = "rscIdPaths")
class RscIdPaths private constructor() : Serializable {
	private var id: String = ""
	private var srvStr = AuthRscAcc.EnSrv.NONE.value
	private var enabled = false
	private var paths: List<String> = mutableListOf()

	fun getId(): String {
		return id
	}

	fun getSrvStr(): String {
		return srvStr
	}

	fun getEnabled(): Boolean {
		return enabled
	}

	fun getPaths(): List<String> {
		return paths
	}

	@Throws(IllegalStateException::class)
	fun getSrv(): AuthRscAcc.EnSrv {
		for (srvEn in AuthRscAcc.EnSrv.values()) {
			if (srvEn.value != srvStr) {
				continue
			}
			return srvEn
		}
		throw IllegalStateException("invalid server '${srvStr}'")
	}


	companion object {
		private const val serialVersionUID = "0.0.1"

		fun withId(id: String): Builder {
			Assert.hasText(id, "id cannot be empty")
			return Builder(id)
		}

		fun from(rscIdPaths: RscIdPaths): Builder {
			return Builder(rscIdPaths)
		}

		class Builder : Serializable {
			companion object {
				private const val serialVersionUID = "0.0.1"
			}
			private var id: String? = null
			private var srv: AuthRscAcc.EnSrv = AuthRscAcc.EnSrv.NONE
			private var enabled: Boolean? = null
			private var paths: List<String>? = null

			constructor(id: String) {
				this.id = id
			}

			constructor(rscIdPaths: RscIdPaths) {
				id = rscIdPaths.id
				srv = rscIdPaths.getSrv()
				enabled = rscIdPaths.enabled
				paths = rscIdPaths.paths
			}

			fun srv(srv: AuthRscAcc.EnSrv): Builder {
				this.srv = srv
				return this
			}

			fun enabled(enabled: Boolean): Builder {
				this.enabled = enabled
				return this
			}

			fun paths(paths: List<String>): Builder {
				this.paths = paths
				return this
			}

			/**
			 * Builds a new [RscIdPaths].
			 *
			 * @return a [RscIdPaths]
			 */
			fun build(): RscIdPaths {
				Assert.hasText(id, "id cannot be empty")
				Assert.state(srv != AuthRscAcc.EnSrv.NONE, "srv cannot be NONE")
				Assert.notNull(paths, "paths cannot be null")
				Assert.state(paths?.isNotEmpty() ?: false, "paths cannot be empty")
				return create()
			}

			private fun create(): RscIdPaths {
				val rscIdPaths = RscIdPaths()
				rscIdPaths.id = id!!
				rscIdPaths.srvStr = srv.value
				rscIdPaths.enabled = enabled ?: false
				rscIdPaths.paths = paths!!
				return rscIdPaths
			}
		}
	}
}
