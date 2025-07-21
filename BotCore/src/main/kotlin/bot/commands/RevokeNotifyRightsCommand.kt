package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class RevokeNotifyRightsCommand(
    private val userService: UserService,
    private val groupService: GroupService
) : BotCommand("revoke_notify_rights", "Отозвать право на отправку уведомлений") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 2) {
            sender.execute(SendMessage(chatId, "⚠️ Формат: /revoke_notify_rights <группа> @username"))
            return
        }

        val groupName = arguments[0].trim()
        val username = arguments[1].removePrefix("@").trim()
        val requester = userService.resolveUser(user)
        val contextChatId = if (chat.isUserChat) null else chatId

        val group = groupService.findByName(groupName, contextChatId, requester)

        println("🚫 Попытка отозвать notify-права:")
        println("👤 Запрос от: ${requester.username} (id=${requester.telegramId})")
        println("👥 Группа: $groupName")
        println("🎯 Цель: @$username")

        if (group == null) {
            sender.execute(SendMessage(chatId, "❌ Группа '${escape(groupName)}' не найдена или недоступна."))
            return
        }

        if (group.owner?.id != requester.id) {
            sender.execute(SendMessage(chatId, "❌ Только владелец может отзывать notify-права."))
            return
        }

        val targetUser = userService.findByUsername(username)
        if (targetUser == null) {
            sender.execute(SendMessage(chatId, "❌ Пользователь @$username не найден."))
            return
        }

        val removed = group.notifiers.removeIf { it.id == targetUser.id }

        if (!removed) {
            sender.execute(SendMessage(chatId, "⚠️ У пользователя @$username не было прав на уведомления."))
            return
        }

        groupService.save(group)

        println("✅ Права пользователя @$username отозваны из группы '${group.name}'")
        sender.execute(SendMessage(chatId, "✅ Пользователь @$username больше не может отправлять уведомления в группу '${escape(group.name)}'."))
    }

    private fun escape(text: String): String {
        val charsToEscape = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        return charsToEscape.fold(text) { acc, c -> acc.replace(c.toString(), "\\$c") }
    }
}
