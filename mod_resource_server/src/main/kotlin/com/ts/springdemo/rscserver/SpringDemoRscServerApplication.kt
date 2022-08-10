package com.ts.springdemo.rscserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableConfigurationProperties(CustomAppProperties::class)
class SpringDemoRscServerApplication

// -----------------------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------

fun main(args: Array<String>) {
	runApplication<SpringDemoRscServerApplication>(*args)
}
