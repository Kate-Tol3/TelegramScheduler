package org.example.restapi.dto

import java.util.*

data class UserDto(
    val id: UUID,
    val telegramId: Long,
    val chatId: String?,
    val username: String?
)

data class CreateUserRequest(
    val telegramId: Long,
    val chatId: String?,
    val username: String?
)
