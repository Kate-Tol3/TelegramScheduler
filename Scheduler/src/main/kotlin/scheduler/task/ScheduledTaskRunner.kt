package org.example.scheduler.task

import org.example.scheduler.kafka.NotificationKafkaProducer
import org.example.scheduler.mapper.toMessage
import org.example.storage.service.ScheduledNotificationService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ScheduledTaskRunner(
    private val scheduledNotificationService: ScheduledNotificationService,
    private val kafkaProducer: NotificationKafkaProducer
) {
    @Scheduled(fixedRate = 60000) // каждый 60 секунд
    fun dispatchDueNotifications() {
        val now = LocalDateTime.now()
        val dueNotifications = scheduledNotificationService.getDueNotificationsWithTargets(now)

        dueNotifications.forEach {
            val message = it.toMessage()
            kafkaProducer.sendNotification(message)
        }

        scheduledNotificationService.markAsDispatched(dueNotifications)
    }
}
