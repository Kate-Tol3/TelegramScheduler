package org.example.bot.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.example.bot.sender.NotificationSender
import org.example.messaging.dto.NotificationMessage
import org.example.storage.service.GroupService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class NotificationKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val notificationSender: NotificationSender,
    private val groupService: GroupService,
    private val absSender: AbsSender // Telegram бот
) {

    @KafkaListener(topics = ["notification-send"], groupId = "bot-group")
    fun consume(record: ConsumerRecord<String, String>) {
        val message = try {
            objectMapper.readValue(record.value(), NotificationMessage::class.java)
        } catch (e: Exception) {
            println("❌ Ошибка десериализации сообщения из Kafka: ${e.message}")
            return
        }

        println("📨 Получено уведомление из Kafka: $message")

        val group = groupService.findByName(message.groupName, message.chatId)
        if (group == null) {
            println("⚠ Группа '${message.groupName}' не найдена для чата ${message.chatId}")
            return
        }

        val chatIdStr = group.chatId
        if (chatIdStr.isNullOrBlank()) {
            println("⚠ У группы '${group.name}' нет chatId")
            return
        }

        val chatId = try {
            chatIdStr.toLong()
        } catch (e: NumberFormatException) {
            println("❌ chatId группы '${group.name}' не является числом: $chatIdStr")
            return
        }

        notificationSender.sendToGroups(absSender, listOf(chatId), message.text)
    }
}
