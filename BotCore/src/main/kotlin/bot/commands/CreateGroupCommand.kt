package org.example.bot.commands

import org.example.storage.service.GroupService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class CreateGroupCommand(
    private val groupService: GroupService
) : BotCommand("create_group", "Создать новую группу") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(SendMessage(chatId, "Укажите название и описание группы: /create_group <group> ; <description>"))
            return
        }

        val fullInput = arguments.joinToString(" ")
        val parts = fullInput.split(";", limit = 2)

        val groupName = parts[0].trim()
        val description = if (parts.size > 1) parts[1].trim() else ""

        if (groupName.isEmpty()) {
            sender.execute(SendMessage(chatId, "Название группы не может быть пустым."))
            return
        }

        if (groupService.findByName(groupName, chatId) != null) {
            sender.execute(SendMessage(chatId, "Группа '$groupName' уже существует."))
            return
        }

        groupService.createGroup(name = groupName, chatId = chatId, description = description)

        sender.execute(SendMessage(chatId, "Группа '$groupName' успешно создана.\nОписание: $description\n\nТеперь вы можете подписаться на неё с помощью /subscribe $groupName"))
    }
}

