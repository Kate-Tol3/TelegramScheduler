package org.example.bot.commands

import org.example.storage.service.TemplateService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class ListTemplatesCommand(
    private val templateService: TemplateService
) : BotCommand("list_templates", "Показать все шаблоны") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()
        val templates = templateService.findAll()

        if (templates.isEmpty()) {
            sender.execute(SendMessage(chatId, "❗ Шаблоны пока не созданы."))
            return
        }

        val grouped = templates.sortedWith(compareBy({ it.eventType.name }, { it.channel.name }))
        val builder = StringBuilder()
        for (template in grouped) {
            builder.appendLine("📌 *${escape(template.eventType.name)}* — `${escape(template.channel.name)}`")
            builder.appendLine("```")
            builder.appendLine(escape(template.text))
            builder.appendLine("```\n")
        }

        val response = SendMessage(chatId, builder.toString().trim()).apply {
            parseMode = "MarkdownV2"
        }

        sender.execute(response)
    }

    private fun escape(text: String): String {
        // Экранируем спецсимволы для MarkdownV2
        return text
            .replace("\\", "\\\\")
            .replace("_", "\\_")
            .replace("*", "\\*")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("~", "\\~")
            .replace("`", "\\`")
            .replace(">", "\\>")
            .replace("#", "\\#")
            .replace("+", "\\+")
            .replace("-", "\\-")
            .replace("=", "\\=")
            .replace("|", "\\|")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace(".", "\\.")
            .replace("!", "\\!")
    }
}
