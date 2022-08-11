package com.ts.springdemo.authserver.service

import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.common.constants.AuthRscAcc
import com.ts.springdemo.authserver.entity.CustomAuthUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class AccessTokenEnhancingService(
			@Autowired
			private val rscIdPathsService: RscIdPathsService
		) {

	fun getAdditionalClaims(authUser: CustomAuthUser?, clientScopes: List<String>): Map<String, Any> {
		val res = HashMap<String, Any>()
		val addRoles: MutableList<String> = mutableListOf()
		if (authUser != null && authUser.getEnabled()) {
			res[AuthRole.CLAIM_AUTHUSER_ROLE_KEY] = authUser.getRole().value.lowercase()
			//
			addRoles.addAll(
					getAdditionalRolesForAuthUser(authUser)
				)
		}
		if ((authUser == null || authUser.getEnabled()) && clientScopes.isNotEmpty()) {
			clientScopes.forEach { itScope: String ->
					val lcScope = itScope.lowercase()
					if (lcScope.startsWith(AuthRscAcc.RSCACC_RULE_PREFIX.lowercase()) &&
							! addRoles.contains(lcScope)) {
						addRoles.add(lcScope)
					}
				}
		}
		if (addRoles.isNotEmpty()) {
			var rolesStr = ""
			addRoles.forEach {
					rolesStr += (if (rolesStr.isEmpty()) { "" } else { "," }) + it
				}
			res[AuthRscAcc.CLAIM_AUTHUSER_RSCACC_ROLES_KEY] = rolesStr
		}
		return res
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun getAdditionalRolesForAuthUser(authUser: CustomAuthUser?): List<String> {
		val res = mutableListOf<String>()
		if (authUser != null) {
			val rscAccMap = authUser.getResourceAccess()
			val rscIdSrvMap = rscIdPathsService.getRscIdToSrvMap(rscAccMap.keys.toList())
			val rulesList = AuthRscAcc.convertMapToRulesList(rscAccMap, rscIdSrvMap)
			rulesList.forEach {
					if (! res.contains(it)) {
						res.add(it)
					}
				}
		}
		return res
	}
}
