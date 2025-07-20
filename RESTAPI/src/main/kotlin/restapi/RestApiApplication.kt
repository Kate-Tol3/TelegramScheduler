// src/main/kotlin/org/example/restapi/RestApiApplication.kt
package org.example.restapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestApiApplication

fun startRestApiApplication(args: Array<String>) {
    runApplication<RestApiApplication>(*args)
}

fun main(args: Array<String>) {
    startRestApiApplication(args)
}
