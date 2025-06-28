package org.example.storage.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

//Уведомление: текст, тип (созвон / МР / релиз), ссылка, дата
@Entity
@Table(name = "notifications")
class Notification(
    @Id @GeneratedValue val id: UUID? = null,

    @Column(nullable = false) val type: String, // MEETING / MR / RELEASE
    @Column(nullable = false) val text: String,
    val link: String?, // опционально — для MRs/созвонов
    val createdAt: LocalDateTime = LocalDateTime.now()
)

