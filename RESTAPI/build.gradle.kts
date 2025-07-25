plugins {
    kotlin("jvm") version "1.9.22"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
//    id("org.jetbrains.kotlin.plugin.spring") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"

//    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":Storage"))
    implementation(project(":KafkaMessaging"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // ✅ добавлено
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    runtimeOnly("org.postgresql:postgresql") // ✅ если используешь PostgreSQL
    // runtimeOnly("com.h2database:h2") // ✅ если тестируешь на H2

    implementation("me.paulschwarz:spring-dotenv:3.0.0")


    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1") // ✅ для Kotlin
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core") // конфликтует с mockito-kotlin
    }

    testImplementation(kotlin("test"))
}


dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

//application {
//    mainClass.set("org.example.restapi.RestApiApplicationKt")
//}

springBoot {
    mainClass.set("org.example.restapi.RestApiApplicationKt")
}

//tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
//    systemProperty("dotenv.directory", "..")
//}


tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.example.restapi.RestApiApplicationKt")
}


//plugins {
//    kotlin("jvm") version "1.9.22"
//    id("org.springframework.boot") version "3.2.0"
//    id("io.spring.dependency-management") version "1.1.4"
//    id("org.jetbrains.kotlin.plugin.spring") version "1.9.22"
//}
//
//group = "org.example"
//version = "1.0-SNAPSHOT"
//
//repositories {
//    mavenCentral()
//}
//
//dependencies {
//    implementation(project(":Storage"))
//    implementation(project(":KafkaMessaging"))
//
//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.kafka:spring-kafka")
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
//    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
//    implementation("me.paulschwarz:spring-dotenv:3.0.0")
//
//    runtimeOnly("org.postgresql:postgresql")
//    testImplementation(kotlin("test"))
//}
//
//dependencyManagement {
//    imports {
//        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
//    }
//}
//
//tasks.test {
//    useJUnitPlatform()
//}
//
//kotlin {
//    jvmToolchain(17)
//}
//
//tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
//    mainClass.set("org.example.restapi.RestApiApplicationKt")
//}
