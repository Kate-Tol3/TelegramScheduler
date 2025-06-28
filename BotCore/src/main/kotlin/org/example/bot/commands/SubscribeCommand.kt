package org.example.bot.commands

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class SubscribeCommand : BotCommand("subscribe", "Подписаться на группу") {
    override fun execute(absSender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        if (arguments.isEmpty()) {
            absSender.execute(SendMessage(chat.id.toString(), "Укажите имя группы: /subscribe <group>"))
            return
        }

        val group = arguments[0]
        // TODO: Реализовать сохранение подписки в БД
        val response = SendMessage(chat.id.toString(), "Вы подписались на группу: $group")
        absSender.execute(response)
    }
}
