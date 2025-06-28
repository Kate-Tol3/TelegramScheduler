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
            sender.execute(SendMessage(chatId, "Укажите название группы: /create_group <group>"))
            return
        }

        val groupName = arguments.joinToString(" ")

        if (groupService.findByName(groupName) != null) {
            sender.execute(SendMessage(chatId, "Группа '$groupName' уже существует."))
            return
        }

        groupService.createGroup(groupName)
        sender.execute(SendMessage(chatId, "Группа '$groupName' успешно создана. Теперь вы можете подписаться на неё с помощью /subscribe $groupName"))
    }
}
