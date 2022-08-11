package com.ts.springdemo.common.constants

import org.springframework.http.HttpMethod


@Suppress("unused")
class AuthRscAcc {
	companion object {
		/** Key in OAuth2 Token claims for the authenticated user's Resource Access roles */
		const val CLAIM_AUTHUSER_RSCACC_ROLES_KEY = "authuser_rscacc_roles"
		/** Prefix for Resource Access rules to a URI-ID and HTTP Method */
		@Suppress("WeakerAccess")
		const val RSCACC_RULE_PREFIX = "resource.uri."

		@Throws(IllegalStateException::class)
		fun buildRule(srv: EnSrv, resourceId: String, httpMethod: EnMeth): String {
			if (srv == EnSrv.NONE) {
				throw IllegalStateException("srv must not be AuthRscAcc.EnSrv.NONE")
			}
			if (resourceId.isEmpty()) {
				throw IllegalStateException("resourceId must not be empty")
			}
			val cleanRscId = cleanUpRscId(resourceId)
			return (RSCACC_RULE_PREFIX + srv.value + ".${cleanRscId}." + httpMethod.value).lowercase()
		}

		fun buildAuthRole(srv: EnSrv, resourceId: String, httpMethod: EnMeth): String {
			return AuthRole.GA_PREFIX + buildRule(srv, resourceId, httpMethod).uppercase()
		}

		fun buildAuthRoleFromArbitraryString(roleStr: String): String {
			return AuthRole.buildAuthRoleFromArbitraryString(roleStr)
		}

		@Throws(IllegalStateException::class)
		fun convertMapToRulesList(resourceAccessMap: Map<String, List<EnMeth>>, rscIdSrvMap: Map<String, EnSrv>): List<String> {
			val res = mutableListOf<String>()
			resourceAccessMap.forEach { (itRaUriId: String, itRaMethList: List<EnMeth>) ->
					itRaMethList.forEach { itRaMeth: EnMeth ->
							val rscSrv: EnSrv = rscIdSrvMap[itRaUriId]
									?: throw IllegalStateException("Resource ID '${itRaUriId}' not found")
							val rule = buildRule(rscSrv, itRaUriId, itRaMeth)
							if (! res.contains(rule)) {
								res.add(rule)
							}
						}
				}
			return res
		}

		private fun cleanUpRscId(rscId: String): String {
			var cpRscId = rscId
			var lastRscId = rscId
			while (true) {
				cpRscId = cpRscId
						.replace(" ", "").replace(".", "_")
						.replace(",", "").replace("'", "")
						.replace(";", "").replace("\"", "")
						.replace("/", "__slash__")
				if (cpRscId == lastRscId) {
					break
				}
				lastRscId = cpRscId
			}
			return cpRscId
		}
	}

	enum class EnMeth(val value: String) {
		ANY("any"),
		GET(HttpMethod.GET.name),
		POST(HttpMethod.POST.name),
		PUT(HttpMethod.PUT.name),
		DELETE(HttpMethod.DELETE.name),
		PATCH(HttpMethod.PATCH.name),
	}

	enum class EnSrv(val value: String) {
		NONE("none"),
		AUTH_SRV("authSrv"),
		RSC_SRV("rscSrv")
	}
}
