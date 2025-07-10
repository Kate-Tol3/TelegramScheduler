//plugins {
//    kotlin("jvm") version "1.9.22"
//    id("org.springframework.boot") version "3.2.0"
//    id("io.spring.dependency-management") version "1.1.4"
//}


//group = "org.example"
//version = "1.0-SNAPSHOT"

//repositories {
//    mavenCentral()
//}

plugins {
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.spring") version "1.9.22" apply false
}

group = "org.example"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral() // 💥 обязательно
    }
}



//dependencies {
//
////        implementation("org.springframework.boot:spring-boot-starter")
////        implementation("org.telegram:telegrambots-spring-boot-starter:6.8.0")
////        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
////        implementation("org.springframework.boot:spring-boot-configuration-processor")
////        implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
////        implementation("org.glassfish.jaxb:jaxb-runtime:3.0.2")
//
////        implementation("org.springframework.boot:spring-boot-starter-data-jpa") // без версии
////        implementation("org.jetbrains.kotlin:kotlin-reflect")
////        implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
//
////        implementation("javax.xml.bind:jaxb-api:2.3.1")
////        implementation("org.glassfish.jaxb:jaxb-runtime:2.3.1")
////
////    testImplementation(kotlin("test"))
//}
//
//tasks.test {
//    useJUnitPlatform()
//}
//kotlin {
//    jvmToolchain(17)
//}