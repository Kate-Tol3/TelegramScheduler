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
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.kafka:spring-kafka")

    // Telegram Bot API + Extensions
    implementation("org.telegram:telegrambotsextensions:6.8.0")


//    implementation("org.telegram:telegrambots-spring-boot-starter:6.8.0")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Jackson for Kotlin support
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    // PostgreSQL JDBC driver
    //implementation("org.postgresql:postgresql")
    implementation("org.postgresql:postgresql:42.7.3")

    // .env support (если используешь dotenv-файлы вручную)
    implementation("me.paulschwarz:spring-dotenv:3.0.0")

    // Подключение модуля Storage
    implementation(project(":Storage"))
    implementation(project(":KafkaMessaging"))
    implementation(project(":RESTAPI"))

    // Тесты
    testImplementation(kotlin("test"))
}


tasks.test {
    useJUnitPlatform()
}

//tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
//    args("--spring.profiles.active=botcore")
//}

tasks.named<JavaExec>("bootRun") {
    environment.putAll(
        mapOf(
            "DOTENV_PATH" to rootDir.absolutePath,
            "DOTENV_FILENAME" to ".env"
        )
    )
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.example.restapi.RestApiApplicationKt"
    }
}


kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("org.example.bot.BotApplicationKt")
}

