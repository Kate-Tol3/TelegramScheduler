package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class GrantAccessCommand(
    private val userService: UserService,
    private val groupService: GroupService
) : BotCommand("grant_access", "Выдать доступ к приватной группе") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 2) {
            sender.execute(SendMessage(chatId, "⚠️ Формат: /grant_access <группа> @username"))
            return
        }

        val groupName = arguments[0].trim()
        val username = arguments[1].removePrefix("@").trim()
        val requester = userService.resolveUser(user)

        val contextChatId = if (chat.isUserChat) {
            groupService.findByOwnerGroupName(requester, groupName)?.chatId
        } else {
            chat.id.toString()
        }
        val group = groupService.findByName(groupName, contextChatId, requester)


        if (group == null) {
            sender.execute(SendMessage(chatId, "❌ Группа '${escape(groupName)}' не найдена или недоступна."))
            return
        }

        if (group.owner?.id != requester.id) {
            sender.execute(SendMessage(chatId, "❌ Только владелец может управлять доступом к группе '${escape(groupName)}'."))
            return
        }

        val targetUser = userService.findByUsername(username)
        if (targetUser == null) {
            sender.execute(SendMessage(chatId, "❌ Пользователь @$username не найден."))
            return
        }

        val alreadyHasAccess = group.allowedUsers.contains(targetUser)
        if (alreadyHasAccess) {
            sender.execute(SendMessage(chatId, "⚠️ Пользователь @$username уже имеет доступ к группе '${escape(groupName)}'."))
            return
        }

        // 👉 Вся логика перемещения в приватную теперь внутри сервиса
        val updatedGroup = groupService.grantAccess(group, targetUser)

        sender.execute(SendMessage(chatId, "✅ Доступ пользователя @$username к группе '${escape(groupName)}' предоставлен."))
    }

    private fun escape(text: String): String {
        val charsToEscape = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        return charsToEscape.fold(text) { acc, c -> acc.replace(c.toString(), "\\$c") }
    }
}
