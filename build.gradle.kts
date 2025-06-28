plugins {
    kotlin("jvm") version "1.9.22"
}


group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    dependencies {
        implementation("me.paulschwarz:spring-dotenv:3.0.0")
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.telegram:telegrambots-spring-boot-starter:6.8.0")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.springframework.boot:spring-boot-configuration-processor")
        implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
        implementation("org.glassfish.jaxb:jaxb-runtime:3.0.2")
    }

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}