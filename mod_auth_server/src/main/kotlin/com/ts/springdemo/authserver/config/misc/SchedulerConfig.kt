package com.ts.springdemo.authserver.config.misc

import com.ts.springdemo.authserver.entity.CustomOAuth2Authorization
import com.ts.springdemo.authserver.repository.CustomOAuth2AuthorizationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.time.Duration
import java.time.Instant


@Configuration
@EnableScheduling
class SchedulerConfig(
			@Autowired
			private val customOAuth2AuthorizationRepository: CustomOAuth2AuthorizationRepository
		) {

	@Scheduled(fixedDelay = 30 * 60 * 1000, initialDelay = 60 * 1000)  // fixedDelay=30min, initialDelay=1min
	fun pruneAccessTokens() {
		val compareTime = Instant.now() - Duration.ofSeconds(120)  // subtracting 2mins for some tolerance
		val res: List<CustomOAuth2Authorization>? = customOAuth2AuthorizationRepository.findByTokenValueExpired(
				compareTime
			)
		res?.forEach {
				if ((it.getTokenAccTokEa() != null && it.getTokenAuthCodeEa() == null &&
							it.getTokenRefrTokEa() == null && it.getTokenOidcTokEa() == null) ||
						(it.getTokenOidcTokEa() != null && it.getTokenOidcTokEa()!! < compareTime)) {
					customOAuth2AuthorizationRepository.deleteById(it.getId())
				}
			}
	}
}
