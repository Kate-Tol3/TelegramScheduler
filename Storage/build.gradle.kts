plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}


group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // JPA + Hibernate
    implementation("org.jetbrains.kotlin:kotlin-reflect") // для работы с аннотациями
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0") // JPA аннотации
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation("org.telegram:telegrambotsextensions:6.8.0")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")


    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Mockito + Kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // Если ещё не добавлено:
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito") // чтобы не конфликтовал с mockito-kotlin
    }

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}