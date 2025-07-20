package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class AllowedUsersCommand(
    private val groupService: GroupService,
    private val userService: UserService
) : BotCommand("allowed_users", "Список пользователей с доступом к приватной группе") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(SendMessage(chatId, "⚠️ Формат: /allowed_users <группа>"))
            return
        }

        val groupName = arguments.joinToString(" ").trim()
        val dbUser = userService.resolveUser(user)

        val contextChatId = if (chat.isUserChat) null else chatId
        val group = groupService.findByName(groupName, contextChatId, dbUser)

        if (group == null || !group.isPrivate) {
            sender.execute(SendMessage(chatId, "❌ Приватная группа '${escape(groupName)}' не найдена или недоступна."))
            return
        }

        if (group.owner?.id != dbUser.id && dbUser !in group.allowedUsers) {
            sender.execute(SendMessage(chatId, "❌ У вас нет прав просматривать список пользователей этой группы."))
            return
        }

        val allowedUsers = group.allowedUsers

        if (allowedUsers.isEmpty()) {
            sender.execute(SendMessage(chatId, "ℹ️ У группы '${escape(groupName)}' нет разрешённых пользователей."))
            return
        }

        val list = allowedUsers.joinToString("\n") {
            val label = it.username?.let { name -> "@$name" } ?: "ID: ${it.telegramId}"
            val prefix = if (it == group.owner) "⭐ " else "- "
            "$prefix$label"
        }

        val text = """
            👥 Пользователи с доступом к группе '${escape(groupName)}':
            $list
        """.trimIndent()

        sender.execute(SendMessage(chatId, text))
    }

    private fun escape(text: String): String {
        val charsToEscape = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        var result = text
        for (char in charsToEscape) {
            result = result.replace(char.toString(), "\\$char")
        }
        return result
    }
}
