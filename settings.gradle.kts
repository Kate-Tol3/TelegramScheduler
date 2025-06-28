plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "TelegramScheduler"
include("bot")
include("Storage")
include("BotCore1")
include("BotCore")
