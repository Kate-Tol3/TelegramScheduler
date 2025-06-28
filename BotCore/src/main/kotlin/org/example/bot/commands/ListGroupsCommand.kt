package org.example.bot.commands

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class ListGroupsCommand : BotCommand("list_groups", "Показать доступные группы") {
    override fun execute(absSender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        // TODO: Получить список групп из БД
        val groups = listOf("backend", "frontend", "devops", "design", "all")
        val response = SendMessage(chat.id.toString(), "Доступные группы:\n" + groups.joinToString("\n"))
        absSender.execute(response)
    }
}
