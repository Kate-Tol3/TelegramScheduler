package org.example.storage.model


import MapToJsonConverter
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.*

//Это реальное событие, которое инициирует отправку сообщения. Может приходить извне через Webhook (из GitLab/GitHub/Jenkins).
@Entity
@Table(name = "events")
class Event(
    @Id @GeneratedValue val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: EventType,

    @Column(columnDefinition = "jsonb", nullable = false)
    @Type(JsonBinaryType::class)
    var payload: Map<String, String>,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)