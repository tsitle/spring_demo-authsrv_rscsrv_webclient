import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("application")
	id("org.springframework.boot") version "2.7.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("kapt") version "1.6.21"  // for @ConfigurationProperties
}

group = "com.ts"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

kapt.includeCompileClasspath=false

application {
	mainClass.set("com.ts.springdemo.authserver.SpringDemoAuthServerApplication")
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":mod_z_common"))
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.security:spring-security-oauth2-authorization-server:0.3.0")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")  // required for /userinfo endpoint
	compileOnly("org.projectlombok:lombok")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.nimbusds:oauth2-oidc-sdk:9.39")  // for com.nimbusds.openid.connect.sdk.OIDCScopeValue
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	annotationProcessor("org.projectlombok:lombok")
	kapt("org.springframework.boot:spring-boot-configuration-processor")  // for @ConfigurationProperties
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
