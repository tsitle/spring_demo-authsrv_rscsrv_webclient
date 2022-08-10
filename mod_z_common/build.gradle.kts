import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar  // for 'val bootJar: BootJar by tasks'

plugins {
	// Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
	kotlin("jvm") version "1.6.21"

	// Apply the java-library plugin for API and implementation separation.
	`java-library`

	id("org.springframework.boot") version "2.7.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("plugin.spring") version "1.6.21"
}

group = "com.ts"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Align versions of all Kotlin components
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.springframework.boot:spring-boot-starter-web")  // for org.springframework.http.HttpMethod
	implementation("org.springframework.boot:spring-boot-starter-security")  // for org.springframework.security.oauth2.core.AuthorizationGrantType
	implementation("org.springframework.security:spring-security-oauth2-authorization-server:0.3.0")  // for org.springframework.security.oauth2.core.AuthorizationGrantType
	implementation("com.nimbusds:oauth2-oidc-sdk:9.39")  // for com.nimbusds.openid.connect.sdk.OIDCScopeValue
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

// disable building a BootJar
tasks.getByName<BootJar>("bootJar") {
	enabled = false
}

tasks.getByName<Jar>("jar") {
	enabled = true
}
