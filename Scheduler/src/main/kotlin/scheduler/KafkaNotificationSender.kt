package org.example.scheduler

import org.example.messaging.dto.NotificationMessage
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaNotificationSender(
    private val kafkaTemplate: KafkaTemplate<String, NotificationMessage>
) {
    fun send(message: NotificationMessage) {
        println("🟢 Scheduler → Kafka: $message")
        kafkaTemplate.send("notification-send", message)
    }
}
