package org.example.bot.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.example.bot.sender.NotificationSender
import org.example.messaging.dto.NotificationMessage
import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class NotificationKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val notificationSender: NotificationSender,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService,
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
            ?: groupService.findByName(message.groupName, null)

        if (group == null) {
            println("❌ Группа '${message.groupName}' не найдена")
            return
        }

        if (group.chatId == null) {
            // 🔹 Глобальная группа — отправляем в тот чат, откуда пришла команда
            val chatId = message.chatId?.toString()?.toLongOrNull()
            if (chatId == null) {
                println("❌ Некорректный chatId для глобальной группы: ${message.chatId}")
                return
            }

            notificationSender.sendToGroups(absSender, listOf(chatId), message.text)

        } else {
            // 🔸 Кастомная группа — отправляем всем подписанным пользователям
            val users = subscriptionService.findUsersByGroup(group)
            val userIds = users.mapNotNull { it.telegramId?.toString()?.toLongOrNull() }

            if (userIds.isEmpty()) {
                println("⚠ У группы '${group.name}' нет подписчиков")
                return
            }

            notificationSender.sendToGroups(absSender, userIds, message.text)
        }

    }


}
