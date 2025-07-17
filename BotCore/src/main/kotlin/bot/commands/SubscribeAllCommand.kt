package org.example.bot.commands

import org.example.storage.service.*
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class SubscribeAllCommand(
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService
) : BotCommand("subscribe_all", "Подписать всех участников чата (доступно только админам)") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val isPrivate = chat.isUserChat
        val chatId = if (isPrivate) {
            if (arguments.isEmpty()) {
                sender.execute(
                    SendMessage(
                        chat.id.toString(),
                        "❌ Укажите ссылку на группу. Пример:\n/subscribe_all https://t.me/groupname"
                    )
                )
                return
            }
            // Преобразуем ссылку https://t.me/groupname в @groupname
            val link = arguments[0].trim()
            if (!link.startsWith("https://t.me/")) {
                sender.execute(SendMessage(chat.id.toString(), "❌ Неверный формат ссылки. Ожидается https://t.me/имя_группы"))
                return
            }
            "@" + link.removePrefix("https://t.me/")
        } else {
            chat.id.toString()
        }

        // Проверка: админ ли вызывающий
        val isAdmin = try {
            val admins = sender.execute(GetChatAdministrators(chatId))
            admins.any { it.user.id == user.id }
        } catch (e: Exception) {
            false
        }

        if (!isAdmin) {
            sender.execute(SendMessage(chat.id.toString(), "⛔ Только администратор указанного чата может выполнить эту команду."))
            return
        }

        // Группа должна быть зарегистрирована
        val group = groupService.findByChatId(chatId)
        if (group == null) {
            sender.execute(SendMessage(chat.id.toString(), "⚠️ Группа с chatId = $chatId не найдена."))
            return
        }

        try {
            val members = sender.execute(GetChatAdministrators(chatId))
                .map { it.user }
                .filter { !it.isBot }

            var count = 0
            for (tgUser in members) {
                val userModel = userService.resolveUser(tgUser)
                val subscribed = subscriptionService.subscribe(userModel, group)
                if (subscribed) count++
            }

            sender.execute(SendMessage(chat.id.toString(), "✅ Подписано $count участников чата '${group.name}'"))
        } catch (e: Exception) {
            sender.execute(SendMessage(chat.id.toString(), "❌ Ошибка при подписке: ${e.message}"))
        }
    }
}
