package org.example.restapi.kafka

import org.example.messaging.dto.NotificationMessage
import org.example.restapi.dto.ScheduleNotificationRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class NotificationKafkaProducer(
    private val immediateKafkaTemplate: KafkaTemplate<String, NotificationMessage>,
    private val scheduleKafkaTemplate: KafkaTemplate<String, ScheduleNotificationRequest>
) {
    fun sendNotification(message: NotificationMessage) {
        println("📤 [Kafka:notification-send] → $message") // ✅ лог
        immediateKafkaTemplate.send("notification-send", message)
    }

    fun sendScheduled(request: ScheduleNotificationRequest) {
        println("⏰ [Kafka:notification-schedule] → $request") // ✅ лог
        scheduleKafkaTemplate.send("notification-schedule", request)
    }
}
