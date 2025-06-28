package org.example.bot.commands

import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand

class StartCommand : BotCommand("start", "Начать работу с ботом") {
    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val message = SendMessage(chat.id.toString(), "Привет! Я бот, который поможет вам получать уведомления.")
        sender.execute(message)
    }
}