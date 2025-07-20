package org.example.bot

import me.paulschwarz.springdotenv.DotenvPropertySource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

//@SpringBootApplication(
//    scanBasePackages = [
//        "org.example.bot",
//        "org.example.storage"
//    ]
//)
@SpringBootApplication(scanBasePackages = ["org.example"])
@EnableJpaRepositories("org.example.storage.repository")
@EntityScan("org.example.storage.model")
class BotApplication

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger(BotApplication::class.java)
    logger.info("🚀 BotCore запущен из main() и инициализирует Spring Boot")
    runApplication<BotApplication>(*args) {
        addInitializers(
            ApplicationContextInitializer<GenericApplicationContext> { context ->
                DotenvPropertySource.addToEnvironment(context.environment)
            }
        )
    }
}
