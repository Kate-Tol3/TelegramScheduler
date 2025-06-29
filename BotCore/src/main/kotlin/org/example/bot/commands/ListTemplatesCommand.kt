package org.example.bot.commands

import org.example.storage.service.TemplateService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.api.objects.User

class ListTemplatesCommand(
    private val templateService: TemplateService
) : BotCommand("list_templates", "Показать все шаблоны") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()
        val templates = templateService.findAll()

        if (templates.isEmpty()) {
            sender.execute(SendMessage(chatId, "Шаблонов нет."))
        } else {
            val text = templates.joinToString("\n\n") { t ->
                "📌 *${t.eventType}* — `${t.channel}`\n${t.text}"
            }
            sender.execute(SendMessage(chatId, text).apply { enableMarkdown(true) })
        }
    }
}
