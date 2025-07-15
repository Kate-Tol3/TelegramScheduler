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
        repeatCount: Int,
        repeatIntervalMinutes: Int,
        event: Event,
        group: Group,
        users: Set<User>
    ): ScheduledNotification {
        val notification = ScheduledNotification(
            template = template,
            event = event,
            eventTime = eventTime,
            repeatCount = repeatCount,
            repeatIntervalMinutes = repeatIntervalMinutes,
            targetGroups = setOf(group),
            targetUsers = users
        )
        return scheduledNotificationRepository.save(notification)
    }

    fun getDueNotificationsWithTargets(now: LocalDateTime): List<ScheduledNotification> {
        return scheduledNotificationRepository.findDueWithTargets(now)
    }




    fun decreaseRepeatOrRemove(notification: ScheduledNotification) {
        if (notification.repeatCount > 1) {
            notification.repeatCount -= 1
            notification.eventTime = notification.eventTime.plusMinutes(notification.repeatIntervalMinutes.toLong())
            save(notification)
        } else {
            delete(notification.id!!)
        }
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
            repeatCount = existing.repeatCount,
            repeatIntervalMinutes = existing.repeatIntervalMinutes,
            targetGroups = existing.targetGroups,
            targetUsers = newUsers
        )

        scheduledNotificationRepository.save(updated)
    }

    fun getDueNotifications(now: LocalDateTime): List<ScheduledNotification> {
        return scheduledNotificationRepository.findAllByEventTimeBeforeAndDispatchedFalse(now)
    }

    fun markAsDispatched(notifications: List<ScheduledNotification>) {
        notifications.forEach { it.dispatched = true }
        scheduledNotificationRepository.saveAll(notifications)
    }




}
