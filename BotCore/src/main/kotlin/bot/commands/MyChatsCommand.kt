package org.example.bot.commands

import org.example.storage.service.GroupService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class MyChatsCommand(
    private val groupService: GroupService
) : BotCommand("my_chats", "Показать чаты, где вы админ") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val allGroups = groupService.findAllWithUsers()
        val userId = user.id
        val adminGroups = mutableListOf<String>()

        for (group in allGroups) {
            val chatId = group.chatId ?: continue
            try {
                val admins = sender.execute(GetChatAdministrators(chatId))
                if (admins.any { it.user.id == userId }) {
                    val displayName = group.name.ifBlank { "Без названия" }
                    adminGroups += "• *${escape(displayName)}* (`$chatId`)"
                }
            } catch (_: Exception) {
                // Бот не в чате или нет доступа к администраторам
            }
        }

        val response = if (adminGroups.isEmpty()) {
            "ℹ️ Вы не являетесь админом ни в одном чате, где состоит бот."
        } else {
            """
                🛡️ *Вы админ в следующих чатах, где состоит бот:*
                
                ${adminGroups.joinToString("\n")}
                
                💡 Вы можете использовать команду:
                `/subscribe_all <chat_id>`
                чтобы подписать всех участников этих чатов.
            """.trimIndent()
        }

        sender.execute(
            SendMessage(chat.id.toString(), response).apply {
                parseMode = "Markdown"
            }
        )
    }

    private fun escape(text: String): String {
        val special = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        return special.fold(text) { acc, char -> acc.replace(char.toString(), "\\$char") }
    }
}
