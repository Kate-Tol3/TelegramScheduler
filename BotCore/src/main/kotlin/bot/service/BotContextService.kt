package org.example.bot.service

import org.example.storage.service.GroupService
import org.telegram.telegrambots.meta.api.objects.Chat
import org.springframework.stereotype.Service

@Service
class BotContextService(
    private val groupService: GroupService
) {
    fun ensureGroupForChat(chat: Chat) {
        val chatId = chat.id.toString()

        // если такая группа уже существует — ничего не делаем
        if (groupService.findByChatId(chatId) != null) return

        val name = chat.title ?: "group_$chatId"

        groupService.createGroup(
            name = name,
            description = "Группа создана автоматически для чата '$name'",
            chatId = chatId
        )

        println("✅ Группа '$name' создана автоматически (chatId = $chatId)")
    }
}
