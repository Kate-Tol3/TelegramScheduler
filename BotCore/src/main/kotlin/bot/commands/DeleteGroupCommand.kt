package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class DeleteGroupCommand(
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService,
    private val userService: UserService
) : BotCommand("delete_group", "Удалить свою группу") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(
                SendMessage(chatId, "⚠️ Укажите имя группы для удаления.\n\nПример:\n`/delete_group backend`")
                    .apply { enableMarkdown(true) }
            )
            return
        }

        val groupName = arguments.joinToString(" ").trim()
        val dbUser = userService.resolveUser(user)

        val group = groupService.findByName(groupName, chatId, dbUser)

        if (group == null) {
            sender.execute(
                SendMessage(chatId, "❌ Группа *$groupName* не найдена или доступ к ней ограничен.")
                    .apply { enableMarkdown(true) }
            )
            return
        }

        if (group.isPrivate && group.owner?.id != dbUser.id) {
            sender.execute(
                SendMessage(chatId, "❌ Только владелец может удалить приватную группу *$groupName*.").apply { enableMarkdown(true) }
            )
            return
        }

        val subscribers = subscriptionService.findUsersByGroup(group)
        if (subscribers.isNotEmpty()) {
            sender.execute(
                SendMessage(chatId, "⛔ Нельзя удалить группу *$groupName*, пока на неё подписаны пользователи (${subscribers.size}).")
                    .apply { enableMarkdown(true) }
            )
            return
        }

        groupService.delete(group)
        sender.execute(SendMessage(chatId, "✅ Группа *$groupName* успешно удалена.").apply { enableMarkdown(true) })
    }
}
