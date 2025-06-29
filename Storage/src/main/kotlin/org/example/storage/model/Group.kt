package org.example.storage.model

import jakarta.persistence.*
import java.util.*

//Группы/роли: фронт, бек, дизайн, все и т.п.
@Entity
@Table(name = "groups")
class Group(
    @Id @GeneratedValue val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false)
    val description: String,

    // Добавлено новое поле:
    @Column(nullable = true)
    val chatId: String? = null // ID чата Telegram для групповой рассылки
)
