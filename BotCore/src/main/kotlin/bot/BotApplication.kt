package org.example.bot

import me.paulschwarz.springdotenv.DotenvPropertySource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = [
        "org.example.bot",
        "org.example.storage"
    ]
)
@EnableJpaRepositories("org.example.storage.repository")
@EntityScan("org.example.storage.model")
class BotApplication

fun main(args: Array<String>) {
    runApplication<BotApplication>(*args) {
        addInitializers(
            ApplicationContextInitializer<GenericApplicationContext> { context ->
                DotenvPropertySource.addToEnvironment(context.environment)
            }
        )
    }
}
