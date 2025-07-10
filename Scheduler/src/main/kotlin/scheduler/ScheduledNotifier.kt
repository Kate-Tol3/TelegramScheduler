package org.example.scheduler

import org.example.messaging.dto.NotificationMessage
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledNotifier(
    private val sender: KafkaNotificationSender
) {
    @Scheduled(fixedRate = 10000) // каждые 10 секунд
    fun sendTestNotification() {
        val msg = NotificationMessage(
            type = "CALL",
            text = "Привет из Scheduler 🎯",
            groupName = "бек",
            chatId = "123456789"
        )
        sender.send(msg)
    }
}
