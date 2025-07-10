package org.example.scheduler.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.messaging.dto.NotificationMessage
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service


@Service
class NotificationKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    fun sendNotification(message: NotificationMessage) {
        val json = objectMapper.writeValueAsString(message)
        kafkaTemplate.send("notification-send", json)
        println("✅ Уведомление отправлено в Kafka: $json")
    }
}

