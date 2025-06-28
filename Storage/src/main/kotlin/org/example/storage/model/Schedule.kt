package org.example.storage.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

//Плановая отложенная отправка уведомления (Содержит запланированное уведомление: когда и кому отправить.)
@Entity
@Table(name = "schedules")
class Schedule(
    @Id @GeneratedValue val id: UUID? = null,

    @ManyToOne @JoinColumn(name = "notification_id")
    val notification: Notification,

    @Column(nullable = false) val scheduledTime: LocalDateTime,
    @Column(nullable = false) val targetType: String, // USER / GROUP
    val targetId: Long
)
