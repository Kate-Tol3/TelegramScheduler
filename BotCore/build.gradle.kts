plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22" // это нужный плагин
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.telegram:telegrambots-spring-boot-starter:6.8.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.telegram:telegrambotsextensions:6.8.0")
    implementation("org.telegram:telegrambots:6.8.0")
    implementation("org.telegram:telegrambots-spring-boot-starter:6.8.0")
    implementation("me.paulschwarz:spring-dotenv:3.0.0")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.1")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.example.BotApplicationKt")
}

