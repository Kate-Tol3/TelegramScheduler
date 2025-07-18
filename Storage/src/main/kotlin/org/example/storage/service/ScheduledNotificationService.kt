package org.example.storage.service

import org.example.storage.model.*
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
        repeatIntervalMinutes: Int,
        event: Event,
        group: Group?, // ← теперь опционально
        users: Set<User>,
        repeatCountUsers: Int,
        repeatCountGroups: Int
    ): ScheduledNotification {

        // 🔒 Валидация параметров
        require(repeatCountUsers >= 0) { "repeatCountUsers не может быть меньше 0" }
        require(repeatCountGroups >= 0) { "repeatCountGroups не может быть меньше 0" }

        val hasRepeats = repeatCountUsers > 0 || repeatCountGroups > 0
        if (hasRepeats && repeatIntervalMinutes <= 0) {
            throw IllegalArgumentException("repeatIntervalMinutes должен быть больше 0 при наличии повторов")
        }

        val notification = ScheduledNotification(
            template = template,
            event = event,
            eventTime = eventTime,
            repeatIntervalMinutes = repeatIntervalMinutes,
            targetGroups = if (group != null) setOf(group) else emptySet(),
            targetUsers = users,
            repeatCountUsers = repeatCountUsers,
            repeatCountGroups = repeatCountGroups,
            totalRepeatCountUsers = repeatCountUsers,
            totalRepeatCountGroups = repeatCountGroups
        )

        return scheduledNotificationRepository.save(notification)
    }




    fun getDueNotificationsWithTargets(now: LocalDateTime): List<ScheduledNotification> {
        return scheduledNotificationRepository.findDueWithTargets(now)
    }

    fun findAllWithUsers(): List<ScheduledNotification> =
        scheduledNotificationRepository.findAllWithUsers()

    fun updateTargetUsers(id: UUID, newUsers: Set<User>) {
        val existing = scheduledNotificationRepository.findById(id).orElseThrow()

        val updated = ScheduledNotification(
            id = existing.id,
            template = existing.template,
            event = existing.event,
            eventTime = existing.eventTime,
            repeatIntervalMinutes = existing.repeatIntervalMinutes,
            totalRepeatCountGroups = existing.totalRepeatCountGroups,
            totalRepeatCountUsers = existing.totalRepeatCountUsers,
            repeatCountGroups = existing.repeatCountGroups,
            repeatCountUsers = existing.repeatCountUsers,
            targetGroups = existing.targetGroups,
            targetUsers = newUsers,
            dispatched = existing.dispatched
        )

        scheduledNotificationRepository.save(updated)
    }

    fun getDueNotifications(now: LocalDateTime): List<ScheduledNotification> {
        return scheduledNotificationRepository.findAllByEventTimeBeforeAndDispatchedFalse(now)
    }

    fun markAsDispatched(notifications: List<ScheduledNotification>) {
        notifications.forEach { notification ->
            if (notification.repeatCountGroups > 0) {
                notification.repeatCountGroups -= 1
            }
            if (notification.repeatCountUsers > 0) {
                notification.repeatCountUsers -= 1
            }

            val stillNeeded = notification.repeatCountGroups > 0 || notification.repeatCountUsers > 0

            if (stillNeeded) {
                notification.eventTime = notification.eventTime.plusMinutes(notification.repeatIntervalMinutes.toLong())
            } else {
                notification.dispatched = true
            }
        }

        scheduledNotificationRepository.saveAll(notifications)
    }
}