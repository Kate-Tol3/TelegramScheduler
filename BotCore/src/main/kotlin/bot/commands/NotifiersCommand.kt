package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class NotifiersCommand(
    private val groupService: GroupService,
    private val userService: UserService
) : BotCommand("notifiers", "Список пользователей с правом на уведомления") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(SendMessage(chatId, "⚠️ Формат: /notifiers <группа>"))
            return
        }

        val groupName = arguments.joinToString(" ").trim()
        val dbUser = userService.resolveUser(user)
        val contextChatId = if (chat.isUserChat) null else chatId

        val group = groupService.findByName(groupName, contextChatId, dbUser)
        if (group == null || !group.isPrivate) {
            sender.execute(SendMessage(chatId, "❌ Приватная группа '$groupName' не найдена или недоступна."))
            return
        }

        if (group.owner?.id != dbUser.id && dbUser !in group.allowedUsers) {
            sender.execute(SendMessage(chatId, "❌ У вас нет прав просматривать список отправителей этой группы."))
            return
        }

        val notifiers = group.notifiers

        if (notifiers.isEmpty()) {
            sender.execute(SendMessage(chatId, "ℹ️ У группы '$groupName' нет назначенных отправителей."))
            return
        }

        val list = notifiers.joinToString("\n") {
            val label = it.username?.let { name -> "@$name" } ?: "ID: ${it.telegramId}"
            val prefix = if (it == group.owner) "⭐ " else "- "
            "$prefix$label"
        }

        val text = """
            📣 Пользователи, которые могут отправлять уведомления в группу '$groupName':
            $list
        """.trimIndent()

        sender.execute(SendMessage(chatId, text))
    }
}
