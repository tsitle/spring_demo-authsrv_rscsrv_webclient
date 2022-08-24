plugins {
	id("org.springframework.boot") version "2.7.1" apply false
	id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false

	// Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin
	kotlin("jvm") version "1.6.21" apply false

	kotlin("plugin.spring") version "1.6.21" apply false

	// for @ConfigurationProperties
	kotlin("kapt") version "1.6.21" apply false
}
