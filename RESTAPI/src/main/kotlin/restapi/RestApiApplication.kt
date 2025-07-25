package org.example.restapi

import me.paulschwarz.springdotenv.DotenvPropertySource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.core.env.ConfigurableEnvironment

@SpringBootApplication
@ComponentScan(basePackages = ["org.example.restapi", "org.example.storage"])
@EnableJpaRepositories("org.example.storage.repository")
@EntityScan("org.example.storage.model")
class RestApiApplication

fun main(args: Array<String>) {
    System.setProperty("dotenv.directory", "..")
    println("🔍 Working dir: " + System.getProperty("user.dir"))
    println("➡ System.getenv = " + System.getenv("RESTAPI_SERVER_PORT"))
    println("➡ System.getProperty = " + System.getProperty("RESTAPI_SERVER_PORT"))
    runApplication<RestApiApplication>(*args) {
        addInitializers(
            ApplicationContextInitializer<GenericApplicationContext> { context ->
                DotenvPropertySource.addToEnvironment(context.environment)
                println("✅ Dotenv loaded. RESTAPI_SERVER_PORT = " + context.environment.getProperty("RESTAPI_SERVER_PORT"))
            }
        )
    }
}




