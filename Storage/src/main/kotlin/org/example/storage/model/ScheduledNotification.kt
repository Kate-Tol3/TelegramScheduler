package org.example.storage.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "scheduled_notifications")
class ScheduledNotification(
    @Id @GeneratedValue val id: UUID? = null,

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    val template: Template,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,  // ← добавлено поле

    @Column(nullable = false)
    var eventTime: LocalDateTime,

    var repeatCount: Int = 0,
    val repeatIntervalMinutes: Int = 0,

    @ManyToMany
    @JoinTable(
        name = "scheduled_notification_groups",
        joinColumns = [JoinColumn(name = "scheduled_notification_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")]
    )
    val targetGroups: Set<Group> = emptySet(),

    @ManyToMany
    @JoinTable(
        name = "scheduled_notification_users",
        joinColumns = [JoinColumn(name = "scheduled_notification_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val targetUsers: Set<User> = emptySet()
)
