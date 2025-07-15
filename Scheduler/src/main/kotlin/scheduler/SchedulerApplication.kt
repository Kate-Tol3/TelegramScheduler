package org.example.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication(scanBasePackages = ["org.example"])
@EnableJpaRepositories(basePackages = ["org.example"])
@EntityScan(basePackages = ["org.example"])
@EnableScheduling
@EnableTransactionManagement // ← вот это добавь
class SchedulerApplication

fun main(args: Array<String>) {
    val app = org.springframework.boot.SpringApplication(SchedulerApplication::class.java)
    app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE) // 🔥 отключает веб
    app.run(*args)
}

