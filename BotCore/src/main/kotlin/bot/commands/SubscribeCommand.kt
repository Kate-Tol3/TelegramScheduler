// ✅ Обновлённый класс SubscribeCommand — подписка на локальные и глобальные группы

package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class SubscribeCommand(
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService
) : BotCommand("subscribe", "Подписаться на группу") {

    override fun execute(
        sender: AbsSender,
        user: User,
        chat: Chat,
        arguments: Array<String>
    ) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(SendMessage(chatId, "Пожалуйста, укажите название группы: /subscribe <group>"))
            return
        }

        val groupName = arguments.joinToString(" ").trim()
        val dbUser = userService.resolveUser(user)

        // 🟢 Сначала пробуем найти локальную группу (по chatId), затем глобальную (chatId = null)
        val dbGroup = groupService.findByName(groupName, chat.id.toString())
            ?: groupService.findByName(groupName, null)

        if (dbGroup == null) {
            sender.execute(
                SendMessage(
                    chatId,
                    "❌ Группа '$groupName' не найдена. Хотите её создать? Напишите /create_group $groupName"
                )
            )
            return
        }

        val subscribed = subscriptionService.subscribe(dbUser, dbGroup)
        val message = if (subscribed) {
            "✅ Вы успешно подписались на группу '$groupName'."
        } else {
            "⚠️ Вы уже подписаны на группу '$groupName'."
        }

        sender.execute(SendMessage(chatId, message))
    }
}
