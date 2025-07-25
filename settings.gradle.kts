//pluginManagement {
//    repositories {
//        gradlePluginPortal()
//        mavenCentral()
//    }
//}
//
//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // 💥 это обязательно!
//    repositories {
//        mavenCentral()
//    }
//}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "TelegramScheduler"

include("Storage")
include("BotCore")
include("Scheduler")
include("KafkaMessaging")
include("RESTAPI")
