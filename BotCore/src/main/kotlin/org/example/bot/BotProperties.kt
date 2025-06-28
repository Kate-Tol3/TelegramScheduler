package org.example.bot

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "bot")
@Component // или регистрируй вручную через @EnableConfigurationProperties(BotProperties::class)
class BotProperties {
    lateinit var token: String
    lateinit var username: String
}

