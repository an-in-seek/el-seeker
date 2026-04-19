plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.elseeker"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val springDocVersion = "2.8.14"
val kotlinLogging = "3.0.5"
val kotestVersion = "5.9.1"
val kotlinJdslVersion = "3.5.5"

dependencies {
    // kotlin-logging
    implementation("io.github.microutils:kotlin-logging:${kotlinLogging}")

    // Spring Boot Framework
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Local cache
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Web UI/UX
    implementation("org.webjars:bootstrap:5.3.0")
    implementation("org.webjars:jquery:3.6.0")

    // i18n
    implementation("com.neovisionaries:nv-i18n:1.29")

    //
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // SpringDoc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springDocVersion}")

    // Kotlin JDSL
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:${kotlinJdslVersion}")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:${kotlinJdslVersion}")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:${kotlinJdslVersion}")

    // DB
    implementation("org.postgresql:postgresql")

    // Google ID Token 검증
    implementation("com.google.api-client:google-api-client:2.7.2")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // Dev
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:${kotestVersion}")
    testImplementation("io.kotest:kotest-assertions-core:${kotestVersion}")
    testImplementation("io.kotest:kotest-framework-engine:${kotestVersion}")

    // testcontainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
