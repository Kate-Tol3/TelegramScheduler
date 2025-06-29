package org.example.storage.service

import org.example.storage.model.ScheduledNotification
import org.example.storage.model.Template
import org.example.storage.model.Event
import org.example.storage.repository.ScheduledNotificationRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class ScheduledNotificationService(
    private val scheduledNotificationRepository: ScheduledNotificationRepository
) {
    fun findById(id: UUID): ScheduledNotification? =
        scheduledNotificationRepository.findById(id).orElse(null)

    fun findAll(): List<ScheduledNotification> = scheduledNotificationRepository.findAll()
    fun save(scheduledNotification: ScheduledNotification): ScheduledNotification =
        scheduledNotificationRepository.save(scheduledNotification)

    fun delete(id: UUID) = scheduledNotificationRepository.deleteById(id)

    fun create(
        template: Template,
        eventTime: LocalDateTime,
        repeatCount: Int,
        repeatIntervalMinutes: Int,
        event: Event
    ): ScheduledNotification {
        val notification = ScheduledNotification(
            id = UUID.randomUUID(),
            template = template,
            eventTime = eventTime,
            repeatCount = repeatCount,
            repeatIntervalMinutes = repeatIntervalMinutes,
            targetGroups = emptySet(), // пока не указываем группы
            targetUsers = emptySet(),  // и пользователей тоже
            event = event
        )
        return scheduledNotificationRepository.save(notification)
    }

}
