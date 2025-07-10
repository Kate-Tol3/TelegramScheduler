package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class UnsubscribeCommand(
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService
) : BotCommand("unsubscribe", "Отписаться от группы") {

    override fun execute(
        sender: AbsSender,
        user: User,
        chat: Chat,
        arguments: Array<String>
    ) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(SendMessage(chatId, "Пожалуйста, укажите название группы: /unsubscribe <group>"))
            return
        }

        val groupName = arguments.joinToString(" ")
        val dbUser = userService.resolveUser(user)
        val dbGroup = groupService.findByName(groupName, chatId)
            ?: groupService.findByName(groupName, null)

        if (dbGroup == null) {
            sender.execute(SendMessage(chatId, "Группа '$groupName' не найдена."))
            return
        }

        val unsubscribed = subscriptionService.unsubscribe(dbUser, dbGroup)
        val message = if (unsubscribed) {
            "Вы успешно отписались от группы '$groupName'."
        } else {
            "Вы не были подписаны на группу '$groupName'."
        }

        sender.execute(SendMessage(chatId, message))
    }
}

