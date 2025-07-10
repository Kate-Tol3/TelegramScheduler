package org.example.bot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "bot")
@Component
class BotProperties {
    lateinit var token: String
    lateinit var username: String
}
