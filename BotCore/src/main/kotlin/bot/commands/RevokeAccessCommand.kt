package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class RevokeAccessCommand(
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService
) : BotCommand("revoke_access", "Отозвать доступ к приватной группе") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 2) {
            sender.execute(SendMessage(chatId, "⚠️ Формат: /revoke_access <группа> @username"))
            return
        }

        val rawUsername = arguments.last().removePrefix("@").trim()
        val rawGroupName = arguments.dropLast(1).joinToString(" ").trim()

        val groupName = rawGroupName.trim()


        val requester = userService.resolveUser(user)
        val group = groupService.findByName(groupName, chatId, requester)

        println("🔍 Отзыв доступа:")
        println("👤 Запрос от: ${requester.username}")
        println("👥 Целевая группа: $groupName")
        println("🎯 Целевой пользователь: @$rawUsername")

        if (group == null || !group.isPrivate) {
            sender.execute(SendMessage(chatId, "❌ Приватная группа '${escape(groupName)}' не найдена или доступ ограничен."))
            return
        }

        if (group.owner?.id != requester.id) {
            sender.execute(SendMessage(chatId, "❌ Только владелец может отзывать доступ к группе '${escape(group.name)}'."))
            return
        }

        val targetUser = userService.findByUsername(rawUsername)
        if (targetUser == null) {
            sender.execute(SendMessage(chatId, "❌ Пользователь @$rawUsername не найден."))
            return
        }

        val hadAccess = group.allowedUsers.removeIf { it.id == targetUser.id }
        group.notifiers.removeIf { it.id == targetUser.id }

        val unsubscribed = subscriptionService.unsubscribe(targetUser, group)
        if (unsubscribed) {
            println("📤 Также отписали пользователя от группы")
        }

        if (!hadAccess) {
            sender.execute(SendMessage(chatId, "⚠️ Пользователь @$rawUsername не имел доступа к группе '${escape(group.name)}'."))
            return
        }

        groupService.save(group)
        sender.execute(SendMessage(chatId, "✅ Доступ пользователя @$rawUsername к группе '${escape(group.name)}' отозван."))
    }

    private fun escape(text: String): String {
        val charsToEscape = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        return charsToEscape.fold(text) { acc, c -> acc.replace(c.toString(), "\\$c") }
    }
}
