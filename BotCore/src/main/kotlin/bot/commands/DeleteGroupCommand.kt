package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.ScheduledNotificationService
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
    private val scheduledNotificationService: ScheduledNotificationService,
    private val userService: UserService
) : BotCommand("delete_group", "Удалить свою группу") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(
                SendMessage(chatId, "⚠️ Укажите имя группы для удаления.\n\nПример:\n/delete_group backend")
                    .apply { enableMarkdown(true) }
            )
            return
        }

        val isConfirmed = arguments.lastOrNull()?.lowercase() == "confirm"
        val groupName = if (isConfirmed) {
            arguments.dropLast(1).joinToString(" ").trim()
        } else {
            arguments.joinToString(" ").trim()
        }

        val dbUser = userService.resolveUser(user)
        val contextChatId = chatId
        var group = groupService.findByName(groupName, contextChatId, dbUser)

        if (group == null && contextChatId != null) {
            val fallback = groupService.findByNameWithUsers(groupName, contextChatId)
            if (fallback != null && fallback.owner?.id == dbUser.id) {
                println("🔁 Fallback: найдена локальная группа пользователя ${dbUser.username}")
                group = fallback
            }
        }


        if (group == null) {
            sender.execute(
                SendMessage(chatId, "❌ Группа *${escape(groupName)}* не найдена или доступ к ней ограничен.")
                    .apply { enableMarkdown(true) }
            )
            return
        }


        if (group.chatId == null && !group.isPrivate) {
            sender.execute(
                SendMessage(chatId, "⛔ Глобальная публичная группа *${escape(group.name)}* не может быть удалена.")
                    .apply { enableMarkdown(true) }
            )
            return
        }

        if (group.owner?.telegramId != user.id) {
            sender.execute(
                SendMessage(chatId, "❌ Только владелец может удалить группу *${escape(group.name)}*.").apply {
                    enableMarkdown(true)
                }
            )
            return
        }

        val subscribers = subscriptionService.findUsersByGroup(group).filter { it.telegramId != user.id }
        val notifications = scheduledNotificationService.findAllWithGroup(group)

        if ((subscribers.isNotEmpty() || notifications.isNotEmpty()) && !isConfirmed) {
            val warning = buildString {
                if (subscribers.isNotEmpty()) {
                    append("⚠️ На группу *${escape(group.name)}* всё ещё подписаны другие пользователи (${subscribers.size}).\n")
                }
                if (notifications.isNotEmpty()) {
                    append("⚠️ С ней связано ${notifications.size} запланированных уведомлений. Они тоже будут удалены.\n")
                }
                append("\nЕсли вы уверены, что хотите удалить её, повторите команду:\n")
                append("/delete_group ${group.name} confirm")
            }

            sender.execute(SendMessage(chatId, warning).apply { enableMarkdown(true) })
            return
        }

        // Удалить связанные уведомления и подписки
        scheduledNotificationService.deleteAllByGroup(group)
        subscriptionService.deleteAllByGroup(group)
        groupService.delete(group)

        sender.execute(
            SendMessage(chatId, "✅ Группа *${escape(group.name)}* успешно удалена.").apply { enableMarkdown(true) }
        )
    }

    private fun escape(text: String): String {
        val charsToEscape = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        return charsToEscape.fold(text) { acc, c -> acc.replace(c.toString(), "\\$c") }
    }
}
