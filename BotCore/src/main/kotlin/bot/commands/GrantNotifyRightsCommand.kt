package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class GrantNotifyRightsCommand(
    private val userService: UserService,
    private val groupService: GroupService
) : BotCommand("grant_notify_rights", "Выдать право на отправку уведомлений в группу") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 2) {
            sender.execute(SendMessage(chatId, "⚠️ Формат: /grant_notify_rights <группа> @username"))
            return
        }

        val rawUsername = arguments.last().removePrefix("@").trim()
        val groupName = arguments.dropLast(1).joinToString(" ").trim()

        val requester = userService.resolveUser(user)
        val contextChatId = if (chat.isUserChat) null else chatId

        val group = groupService.findByName(groupName, contextChatId, requester)
        if (group == null) {
            sender.execute(SendMessage(chatId, "❌ Группа '${escape(groupName)}' не найдена или недоступна."))
            return
        }

        if (group.owner?.id != requester.id) {
            sender.execute(SendMessage(chatId, "❌ Только владелец может выдавать права на уведомления."))
            return
        }

        val targetUser = userService.findByUsername(rawUsername)
        if (targetUser == null) {
            sender.execute(SendMessage(chatId, "❌ Пользователь @$rawUsername не найден."))
            return
        }

        if (group.notifiers.any { it.id == targetUser.id }) {
            sender.execute(SendMessage(chatId, "⚠️ Пользователь @$rawUsername уже может отправлять уведомления."))
            return
        }

        val isListener = targetUser.id == group.owner?.id || group.allowedUsers.any { it.id == targetUser.id }
        if (!isListener) {
            sender.execute(SendMessage(chatId, "⚠️ Сначала вы должны выдать доступ через /grant_access."))
            return
        }

        val updatedGroup = groupService.grantNotifyRights(group, targetUser)
        // Название группы больше НЕ экранируется
        sender.execute(
            SendMessage(chatId, "✅ Пользователь @$rawUsername теперь может отправлять уведомления в группу '${updatedGroup.name}'.")
        )
    }

    private fun escape(text: String): String {
        val charsToEscape = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        return charsToEscape.fold(text) { acc, c -> acc.replace(c.toString(), "\\$c") }
    }
}
