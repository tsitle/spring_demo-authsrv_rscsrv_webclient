package com.ts.springdemo.oauthwebclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableConfigurationProperties(CustomAppProperties::class)
class SpringDemoOauthWebClientApplication

// -----------------------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------

fun main(args: Array<String>) {
	runApplication<SpringDemoOauthWebClientApplication>(*args)
}
