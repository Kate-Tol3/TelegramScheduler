package org.example.bot.sender

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
            result = result.replace("{{${key}}}", value)
        }
        return result
    }
}
