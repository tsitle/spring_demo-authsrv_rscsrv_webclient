package com.ts.springdemo.authserver.service

import com.ts.springdemo.authserver.entity.RscIdPaths
import com.ts.springdemo.authserver.repository.RscIdPathsRepository
import com.ts.springdemo.common.constants.AuthRscAcc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*


@Service
class RscIdPathsService(
			@Autowired
			private val rscIdPathsRepository: RscIdPathsRepository
		) {

	@Throws(IllegalStateException::class)
	fun getRscIdToSrvMap(rscIds: List<String>): Map<String, AuthRscAcc.EnSrv> {
		val res = mutableMapOf<String, AuthRscAcc.EnSrv>()
		rscIds.forEach { itRscId: String ->
				val dbRscIdPaths: Optional<RscIdPaths?> = rscIdPathsRepository.findById(itRscId)
				if (dbRscIdPaths.isEmpty) {
					throw IllegalStateException("Resource ID '${itRscId}' not found in DB")
				}
				res[itRscId] = dbRscIdPaths.get().getSrv()
			}
		return res
	}
}
