package org.example.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import me.paulschwarz.springdotenv.DotenvPropertySource
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories("org.example.bot.repository")
@EntityScan("org.example.bot.model")
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

