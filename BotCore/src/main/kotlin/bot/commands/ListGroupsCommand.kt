// ✅ ListGroupsCommand: разделение групп на локальные и глобальные

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
        if (groups.isEmpty()) {
            sender.execute(SendMessage(chatId, "Пока нет доступных групп."))
            return
        }

        val (globalGroups, localGroups) = groups.partition { it.chatId == null }

        val builder = StringBuilder()
        if (localGroups.isNotEmpty()) {
            builder.append("📍 Локальные группы:")
            localGroups.forEach { builder.append("\n- ${it.name}") }
            builder.append("\n\n")
        }
        if (globalGroups.isNotEmpty()) {
            builder.append("🌐 Глобальные группы:")
            globalGroups.forEach { builder.append("\n- ${it.name}") }
        }

        sender.execute(SendMessage(chatId, builder.toString().trim()))
    }
}