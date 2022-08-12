package com.ts.springdemo.authserver.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class CustomAuthenticationProvider : AuthenticationProvider {
	@Autowired
	private val customAuthUserService: CustomAuthUserService? = null

	@Autowired
	private val passwordEncoder: PasswordEncoder? = null

	@Throws(UsernameNotFoundException::class, BadCredentialsException::class)
	override fun authenticate(authentication: Authentication): Authentication {
		val username = authentication.name
		val password = authentication.credentials.toString()
		val user: UserDetails = customAuthUserService?.loadUserByUsername(username)
				?: throw UsernameNotFoundException("No matching User found")
		if (! passwordEncoder!!.matches(password, user.password)) {
			throw BadCredentialsException("Bad Credentials")
		}
		return UsernamePasswordAuthenticationToken(
				user.username,
				user.password,
				user.authorities
			)
	}

	override fun supports(authentication: Class<*>): Boolean {
		return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
	}
}
