package org.example.bot.commands

import org.example.storage.service.GroupService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class DeleteGroupCommand(
    private val groupService: GroupService
) : BotCommand("delete_group", "Удалить пользовательскую группу") {

    private val systemGroups = setOf("backend", "frontend", "devops", "design", "all")

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(SendMessage(chatId, "⚠️ Укажи имя группы для удаления. Пример: /delete_group разработка"))
            return
        }

        val groupName = arguments.joinToString(" ").trim()

        if (groupName.lowercase() in systemGroups) {
            sender.execute(SendMessage(chatId, "❌ Нельзя удалить системную группу '$groupName'"))
            return
        }

        val group = groupService.findByName(groupName, chatId)

        if (group == null) {
            sender.execute(SendMessage(chatId, "❌ Группа '$groupName' не найдена в этом чате"))
            return
        }

        groupService.delete(group) // ✅



        sender.execute(SendMessage(chatId, "✅ Группа '$groupName' успешно удалена"))
    }
}
