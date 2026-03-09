package org.example.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication(scanBasePackages = ["org.example"])
@EnableJpaRepositories(basePackages = ["org.example"])
@ComponentScan(basePackages = ["org.example"])
@EntityScan(basePackages = ["org.example"])
@EnableScheduling
@EnableTransactionManagement
class SchedulerApplication

fun main(args: Array<String>) {
    System.setProperty("dotenv.directory", "..") // поднимаемся на уровень выше, если .env в корне проекта
    val app = org.springframework.boot.SpringApplication(SchedulerApplication::class.java)
    app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE) //отключает веб
    app.run(*args)
}

