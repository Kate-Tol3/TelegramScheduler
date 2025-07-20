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
    private val absSender: AbsSender
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

        // ❗ Теперь используем метод с 3 аргументами (user = null)
        val group = groupService.findByName(message.groupName, message.chatId, null)

        if (group == null) {
            println("❌ Группа '${message.groupName}' не найдена или доступ к ней ограничен")
            return
        }

        // 🔹 Отправка в групповой чат
        if (message.sendToGroup) {
            val chatId = group.chatId ?: message.chatId
            val resolvedChatId = chatId?.toLongOrNull()
            if (resolvedChatId != null) {
                notificationSender.sendToGroups(absSender, listOf(resolvedChatId), message.text)
            } else {
                println("⚠️ Не удалось определить chatId для отправки в группу: ${message.chatId}")
            }
        }

        // 🔸 Отправка подписанным пользователям
        if (message.sendToUsers) {
            val users = subscriptionService.findUsersByGroup(group)
            if (users.isEmpty()) {
                println("⚠ У группы '${group.name}' нет подписчиков")
            } else {
                notificationSender.sendToUsers(absSender, users, message.text)
            }
        }
    }
}
