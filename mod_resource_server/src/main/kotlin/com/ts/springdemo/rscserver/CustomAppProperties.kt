package com.ts.springdemo.rscserver

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


/**
 * In IntelliJ IDEA:
 *   - Make sure Spring Boot plugin in enabled in menu File | Settings | Plugins | Spring Boot
 *   - Enable annotation processing via menu File | Settings | Build, Execution, Deployment | Compiler | Annotation Processors | Enable annotation processing
 * Since Kapt is not yet integrated in IDEA, you need to run manually the command
 *   $ ./gradlew kaptKotlin
 * to generate the metadata
 */
@ConstructorBinding
@ConfigurationProperties("custom-app")
data class CustomAppProperties(
			val authServer: AuthServer
		) {

	data class AuthServer(val providerIssuerUrl: String = "")
}
