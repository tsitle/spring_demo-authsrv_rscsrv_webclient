package com.ts.springdemo.authserver.service

import com.ts.springdemo.common.constants.AuthRole
import com.ts.springdemo.common.constants.AuthRscAcc
import com.ts.springdemo.authserver.entity.CustomAuthUser
import com.ts.springdemo.authserver.repository.CustomAuthUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service
class CustomUserDetailsService(
			@Autowired
			private val customAuthUserRepository: CustomAuthUserRepository,
			@Autowired
			private val rscIdPathsService: RscIdPathsService
		) : UserDetailsService {

	@Throws(UsernameNotFoundException::class)
	override fun loadUserByUsername(email: String): UserDetails {
		val user: CustomAuthUser = customAuthUserRepository.findByEmail(email) ?:
				throw UsernameNotFoundException("No matching User found")
		return org.springframework.security.core.userdetails.User(
				user.getEmail(),
				user.getPassword(),
				user.getEnabled(),
				true,
				true,
				true,
				getAuthorities(user)
			)
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun getAuthorities(user: CustomAuthUser): Collection<GrantedAuthority> {
		val authorities: MutableList<GrantedAuthority> = ArrayList()
		//
		authorities.add(
				SimpleGrantedAuthority(
						AuthRole.buildAuthRole(user.getRole())
					)
			)
		//
		val rscIdSrvMap = rscIdPathsService.getRscIdToSrvMap(user.getResourceAccess().keys.toList())
		user.getResourceAccess().forEach { (itRscId: String, itRscMeths: List<AuthRscAcc.EnMeth>) ->
				itRscMeths.forEach { itRscMeth: AuthRscAcc.EnMeth ->
						val srv: AuthRscAcc.EnSrv = rscIdSrvMap[itRscId]!!
						val sga = SimpleGrantedAuthority(
								AuthRscAcc.buildAuthRole(srv, itRscId, itRscMeth)
							)
						authorities.add(sga)
					}
			}
		return authorities
	}
}
