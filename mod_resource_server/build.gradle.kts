import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("application")
	id("org.springframework.boot")  // the version has been defined in /build.gradle.kts
	id("io.spring.dependency-management")  // the version has been defined in /build.gradle.kts

	// Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin
	kotlin("jvm")  // the version has been defined in /build.gradle.kts

	kotlin("plugin.spring")  // the version has been defined in /build.gradle.kts

	// for @ConfigurationProperties
	kotlin("kapt")  // the version has been defined in /build.gradle.kts
}

group = "com.ts"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

application {
	// the class name needs to be appended with "Kt" so that gradle knows that it is a Kotlin application
	mainClass.set("com.ts.springdemo.rscserver.SpringDemoRscServerApplicationKt")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":mod_z_common"))
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework:spring-webflux")
	implementation("io.projectreactor.netty:reactor-netty")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.nimbusds:oauth2-oidc-sdk:9.39")  // for com.nimbusds.openid.connect.sdk.OIDCScopeValue
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	kapt("org.springframework.boot:spring-boot-configuration-processor")  /* for @ConfigurationProperties */
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
