package org.example.restapi.controller

import org.example.messaging.dto.NotificationMessage
import org.example.restapi.dto.ScheduleNotificationRequest
import org.example.restapi.dto.SendImmediateNotificationRequest

import org.example.restapi.kafka.NotificationKafkaProducer
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val kafkaProducer: NotificationKafkaProducer
) {

    /**
     * 📤 Мгновенная рассылка уведомления
     * Отправляет сообщение в Kafka (notification-send), обрабатывается в BotCore
     */
    @PostMapping("/immediate")
    fun sendImmediate(@RequestBody request: SendImmediateNotificationRequest) {
        val message = NotificationMessage(
            text = request.text,
            groupName = request.groupName,
            chatId = null,
            sendToUsers = request.private,
            sendToGroup = request.chat,
            type = "IMMEDIATE" // или другой подходящий тип
        )
        kafkaProducer.sendNotification(message)
    }

    /**
     * ⏰ Отложенная рассылка уведомления с повторами
     * Отправляет ScheduleNotificationRequest в Kafka (notification-schedule), обрабатывается в BotCore
     */
    @PostMapping("/schedule")
    fun schedule(@RequestBody request: ScheduleNotificationRequest) {
        kafkaProducer.sendScheduled(request)
    }
}
