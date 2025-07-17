// ✅ StartCommand: теперь выводит приветствие + список доступных команд (как /help)

package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand

class StartCommand(
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService,
    private val userService: UserService
) : BotCommand("start", "Начать работу с ботом") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()
        val dbUser = userService.resolveUser(user)

        // ✅ Глобальные группы при старте
        val defaultGroups = listOf("backend", "frontend", "devops", "design", "all")
        val globalGroups = defaultGroups.mapNotNull { name ->
            groupService.findByName(name, null) ?: groupService.createGroup(
                name = name,
                description = "Глобальная группа $name",
                chatId = null
            )
        }

        // ✅ Приветствие + список команд (как /help)
        val greeting = buildString {
            append("Привет! Я бот, который поможет вам получать уведомления.\n\n")
            append("/help - Показать список всех команд\n")
        }

        sender.execute(SendMessage(chatId, greeting))

        // ✅ Предложение подписки в ЛС
        if (chat.isUserChat) {
            val userSubscriptions = subscriptionService.findByUser(dbUser)
            if (userSubscriptions.isEmpty()) {
                val msg = buildString {
                    append("\nВы можете подписаться на глобальные группы:\n")
                    globalGroups.forEach { append("• ${it.name}\n") }
                    append("\nДля подписки используйте команду:\n/subscribe <название группы>")
                }
                sender.execute(SendMessage(chatId, msg))
            }
        }
    }
}