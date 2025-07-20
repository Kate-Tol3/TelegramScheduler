package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class UnsubscribeCommand(
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService
) : BotCommand("unsubscribe", "Отписаться от группы") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(SendMessage(chatId, "Пожалуйста, укажите название группы: /unsubscribe <group>"))
            return
        }

        val groupName = arguments.joinToString(" ").trim()
        val dbUser = userService.resolveUser(user)

        println("🔍 Попытка отписки:")
        println("👤 Пользователь: ${dbUser.username} (id=${dbUser.telegramId})")
        println("💬 Название группы: $groupName")
        println("💬 Откуда вызвана команда: chatId=$chatId")
        println("💬 Chat type: ${chat.type}")

        // Для ЛС контекст поиска должен быть null (глобальные и приватные)
        val contextChatId = if (chat.isUserChat) null else chatId

        val group = groupService.findByName(groupName, contextChatId, dbUser)

        if (group == null) {
            println("❌ Группа не найдена или доступ ограничен")
            sender.execute(SendMessage(chatId, "❌ Группа '$groupName' не найдена или доступ к ней ограничен."))
            return
        }

        val unsubscribed = subscriptionService.unsubscribe(dbUser, group)
        val message = if (unsubscribed) {
            "✅ Вы успешно отписались от группы '${group.name}'."
        } else {
            "⚠️ Вы не были подписаны на группу '${group.name}'."
        }

        println("📩 $message")
        sender.execute(SendMessage(chatId, message))
    }
}
