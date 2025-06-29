package org.example.bot.commands

import org.example.storage.service.GroupService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class ListGroupsCommand(
    private val groupService: GroupService
) : BotCommand("list_groups", "Показать все доступные группы") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()
        val groups = groupService.findAll()
        val messageText = if (groups.isEmpty()) {
            "Пока нет доступных групп."
        } else {
            "Доступные группы:\n" + groups.joinToString("\n") { "- ${it.name}" }
        }

        sender.execute(SendMessage(chatId, messageText))
    }
}
