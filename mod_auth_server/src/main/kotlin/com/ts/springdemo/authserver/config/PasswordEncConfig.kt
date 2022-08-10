package com.ts.springdemo.authserver.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder


@Configuration
class PasswordEncConfig {
	@Bean
	@Qualifier("passwordEncoder")
	fun passwordEncoder(): PasswordEncoder? {
		return BCryptPasswordEncoder(11)
	}
}
