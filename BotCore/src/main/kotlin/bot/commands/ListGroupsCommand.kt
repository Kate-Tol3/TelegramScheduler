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
        val allGroups = groupService.findAll()

        val globalGroups = allGroups.filter { it.chatId == null }
        val localGroups = allGroups.filter { it.chatId == chatId }

        if (globalGroups.isEmpty() && localGroups.isEmpty()) {
            sender.execute(SendMessage(chatId, "Пока нет доступных групп."))
            return
        }

        val builder = StringBuilder()

        if (localGroups.isNotEmpty()) {
            builder.appendLine("📍 *Ваши локальные группы:*")
            localGroups.forEach { builder.appendLine("- ${it.name}") }
            builder.appendLine()
        }

        if (globalGroups.isNotEmpty()) {
            builder.appendLine("🌐 *Глобальные группы:*")
            globalGroups.forEach { builder.appendLine("- ${it.name}") }
        }

        sender.execute(SendMessage(chatId, builder.toString().trim()).apply {
            parseMode = "Markdown"
        })
    }
}
