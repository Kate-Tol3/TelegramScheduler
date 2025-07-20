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

        val groupName = arguments[0].trim()
        val username = arguments[1].removePrefix("@").trim()
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

        val targetUser = userService.findByUsername(username)
        if (targetUser == null) {
            sender.execute(SendMessage(chatId, "❌ Пользователь @$username не найден."))
            return
        }

        if (!group.allowedUsers.contains(targetUser)) {
            sender.execute(SendMessage(chatId, "⚠️ Сначала вы должны выдать доступ к группе через /grant_access."))
            return
        }

        if (group.notifiers.contains(targetUser)) {
            sender.execute(SendMessage(chatId, "⚠️ Пользователь @$username уже имеет право отправлять уведомления."))
            return
        }

        // 🔁 Если группа локальная и это не системная глобальная — делаем приватной
        if (!group.isPrivate && group.chatId != null && targetUser != group.owner) {
            println("🔁 Группа '${group.name}' становится приватной глобальной (chatId = null, isPrivate = true)")
            group.chatId = null
            group.isPrivate = true
        }

        val updatedGroup = groupService.grantNotifyRights(group, targetUser)

        println("✅ @$username (id=${targetUser.telegramId}) добавлен в notifiers группы '${group.name}'")
        println("✅ Текущие notifiers: ${updatedGroup.notifiers.map { it.username }}")

        sender.execute(SendMessage(chatId, "✅ Пользователь @$username теперь может отправлять уведомления в группу '${escape(groupName)}'."))
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
