package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class SubscribeCommand(
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService
) : BotCommand("subscribe", "Подписаться на группу") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(SendMessage(chatId, "⚠️ Укажите название группы: /subscribe <group>"))
            return
        }

        val groupName = arguments.joinToString(" ").trim()
        val dbUser = userService.resolveUser(user)
        val contextChatId = if (chat.isUserChat) null else chatId

        println("🔍 Попытка подписки:")
        println("👤 Пользователь: ${dbUser.username} (id=${dbUser.telegramId})")
        println("💬 Название группы: $groupName")
        println("💬 Откуда вызвано: chatId=$chatId, Тип: ${chat.type}")

        val group = groupService.findByName(groupName, contextChatId, dbUser)

        if (group == null) {
            sender.execute(SendMessage(chatId, "❌ Группа '${escape(groupName)}' не найдена или доступ к ней ограничен."))
            return
        }

        println("🟢 Найдена группа '${group.name}' (chatId=${group.chatId}, isPrivate=${group.isPrivate})")

        val hasAccess = when {
            group.chatId == chatId -> true
            group.chatId == null && !group.isPrivate -> true
            group.chatId == null && group.isPrivate && group.allowedUsers.any { it.id == dbUser.id } -> true
            else -> false
        }

        if (!hasAccess) {
            sender.execute(SendMessage(chatId, "❌ У вас нет доступа к группе '${escape(group.name)}'"))
            return
        }

        val subscribed = subscriptionService.subscribe(dbUser, group)
        val result = if (subscribed) {
            "✅ Вы успешно подписались на группу '${escape(group.name)}'."
        } else {
            "⚠️ Вы уже подписаны на группу '${escape(group.name)}'."
        }

        sender.execute(SendMessage(chatId, result))
    }

    private fun escape(text: String): String {
        val charsToEscape = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        return charsToEscape.fold(text) { acc, c -> acc.replace(c.toString(), "\\$c") }
    }
}
