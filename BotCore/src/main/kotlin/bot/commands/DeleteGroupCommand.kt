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

        val group = groupService.findByName(groupName, if (chat.isUserChat) null else chatId, dbUser)

        if (group == null) {
            sender.execute(SendMessage(chatId, "❌ Группа *$groupName* не найдена или доступ к ней ограничен.")
                .apply { enableMarkdown(true) })
            return
        }

        // 🔒 Запрет на удаление глобальных публичных групп
        if (group.chatId == null && !group.isPrivate) {
            sender.execute(SendMessage(chatId, "⛔ Глобальная публичная группа *$groupName* не может быть удалена.")
                .apply { enableMarkdown(true) })
            return
        }

        // 🔒 Только владелец может удалить локальную или приватную группу
        if (group.owner?.id != dbUser.id) {
            sender.execute(SendMessage(chatId, "❌ Только владелец может удалить группу *$groupName*.").apply { enableMarkdown(true) })
            return
        }

        val subscribers = subscriptionService.findUsersByGroup(group).filter { it.id != dbUser.id }

        if (subscribers.isNotEmpty()) {
            sender.execute(
                SendMessage(chatId, """
                    ⚠️ На группу *$groupName* всё ещё подписаны другие пользователи (${subscribers.size}).
                    Если вы уверены, что хотите удалить её, повторите команду:  
                    `/delete_group $groupName confirm`
                """.trimIndent()).apply { enableMarkdown(true) }
            )
            return
        }

        // Проверка подтверждения удаления, если подписчики всё ещё есть
        if (arguments.size >= 2 && arguments[1] != "confirm") {
            sender.execute(SendMessage(chatId, "❗ Для удаления группы добавьте `confirm` после имени.").apply { enableMarkdown(true) })
            return
        }

        groupService.delete(group)
        sender.execute(SendMessage(chatId, "✅ Группа *$groupName* успешно удалена.").apply { enableMarkdown(true) })
    }
}
