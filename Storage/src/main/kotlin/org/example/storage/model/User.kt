package org.example.storage.model

import jakarta.persistence.*
import java.util.UUID

// Telegram-пользователь, которому можно отправлять уведомления
@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val telegramId: Long,

    @Column(nullable = false)
    val chatId: String, // ← нужно для отправки сообщений

    @Column(nullable = false)
    val username: String,
)
