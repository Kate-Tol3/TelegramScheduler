package org.example.restapi.mapper

import org.example.restapi.dto.UserDto
import org.example.storage.model.User

fun User.toDto(): UserDto = UserDto(
    id = this.id!!,
    telegramId = this.telegramId,
    chatId = this.chatId,
    username = this.username
)
