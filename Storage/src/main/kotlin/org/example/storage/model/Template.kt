package org.example.storage.model

import jakarta.persistence.*
import java.util.*

//Хранит шаблон сообщения, которое будет отправлено пользователям в зависимости от события: созвона, Merge Request или релиза.
@Entity
@Table(name = "templates")
class Template(
    @Id @GeneratedValue val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val eventType: EventType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val channel: Channel,

    @Column(nullable = false)
    val text: String
)

enum class Channel {
    GROUP, PRIVATE, BOTH
}
