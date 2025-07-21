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
            sender.execute(
                SendMessage(chatId, "⚠️ Формат: /grant_access <группа> @username")
            )
            return
        }

        // Выделяем username и groupName с очисткой
        val rawUsername = arguments.last().removePrefix("@").trim()
        val rawGroupName = arguments.dropLast(1).joinToString(" ").trim()

        val groupName = rawGroupName.trim()


        val requester = userService.resolveUser(user)

        val contextChatId = if (chat.isUserChat) {
            groupService.findByOwnerGroupName(requester, groupName)?.chatId
        } else {
            chat.id.toString()
        }

        val group = groupService.findByName(groupName, contextChatId, requester)
        if (group == null) {
            sender.execute(
                SendMessage(chatId, "❌ Группа \"$groupName\" не найдена или недоступна.")
            )
            return
        }

        if (group.owner?.id != requester.id) {
            sender.execute(
                SendMessage(chatId, "❌ Только владелец может управлять доступом к группе \"${group.name}\".")
            )
            return
        }

        val targetUser = userService.findByUsername(rawUsername)
        if (targetUser == null) {
            sender.execute(
                SendMessage(chatId, "❌ Пользователь @$rawUsername не найден.")
            )
            return
        }

        val allGroups = groupService.findAllByName(group.name)
        val conflict = allGroups.any {
            it.owner?.id == requester.id &&
                    (it.owner?.id == targetUser.id || it.allowedUsers.any { u -> u.id == targetUser.id })
        }

        if (conflict) {
            sender.execute(
                SendMessage(chatId, "⚠️ У пользователя @$rawUsername уже есть доступ к группе с таким названием и владельцем.")
            )
            return
        }

        var renamed = false
        if (!group.isPrivate && group.chatId != null) {
            val newName = "${group.name} [от @${requester.username}]"
            group.name = newName
            renamed = true
        }

        val updatedGroup = groupService.grantAccess(group, targetUser)

        val notice = buildString {
            append("✅ Доступ пользователя @$rawUsername к группе \"${updatedGroup.name}\" предоставлен.")
            if (renamed) {
                appendLine()
                append("ℹ️ Название группы изменено на \"${updatedGroup.name}\", чтобы обеспечить уникальность.")
            }
        }

        sender.execute(SendMessage(chatId, notice))
    }
}
