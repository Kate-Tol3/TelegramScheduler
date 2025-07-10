package org.example.messaging.dto

data class NotificationMessage(
    val type: String,
    val text: String,
    val groupName: String,
    val chatId: String? = null // ✅ новое поле
)

