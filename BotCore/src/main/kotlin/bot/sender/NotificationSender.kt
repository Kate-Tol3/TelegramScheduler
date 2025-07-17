package org.example.bot.sender

import org.example.storage.model.ScheduledNotification
import org.example.storage.model.Template
import org.example.storage.model.User
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import org.springframework.stereotype.Component

@Component
class NotificationSender {

    fun sendToUsers(sender: AbsSender, users: Collection<User>, message: String) {
        users.forEach { user ->
            val msg = SendMessage(user.telegramId.toString(), message)
            sender.execute(msg)
        }
    }

    fun sendToGroups(sender: AbsSender, groupChatIds: Collection<Long>, message: String) {
        groupChatIds.forEach { chatId ->
            val msg = SendMessage(chatId.toString(), message)
            sender.execute(msg)
        }
    }

    fun applyTemplate(template: Template, placeholders: Map<String, String>): String {
        var result = template.text
        placeholders.forEach { (key, value) ->
            result = result.replace("{$key}", value) // 🔄 заменили {{key}} на {key}
        }
        return result
    }

//    fun sendScheduledNotification(
//        sender: AbsSender,
//        notification: ScheduledNotification
//    ) {
//        val message = applyTemplate(notification.template, notification.event.payload)
//
//        // отправка в группы
//        if (notification.repeatCountGroups > 0) {
//            val groupChatIds = notification.targetGroups.mapNotNull { it.chatId?.toLongOrNull() }
//            sendToGroups(sender, groupChatIds, message)
//        }
//
//        // отправка пользователям
//        if (notification.repeatCountUsers > 0) {
//            sendToUsers(sender, notification.targetUsers, message)
//        }
//    }

}
