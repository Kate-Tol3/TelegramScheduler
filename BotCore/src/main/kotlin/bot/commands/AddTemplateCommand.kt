package org.example.bot.commands

import org.example.storage.model.Channel
import org.example.storage.model.EventType
import org.example.storage.model.Template
import org.example.storage.service.TemplateService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class AddTemplateCommand(
    private val templateService: TemplateService
) : BotCommand("add_template", "Добавить новый шаблон уведомления") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 3) {
            sender.execute(
                SendMessage(
                    chatId,
                    "Использование:\n/add_template <CALL|MR|RELEASE> <GROUP|PRIVATE|BOTH> <текст шаблона>"
                )
            )
            return
        }

        val eventType = try {
            EventType.valueOf(arguments[0].uppercase())
        } catch (e: IllegalArgumentException) {
            sender.execute(SendMessage(chatId, "Неверный тип события: ${arguments[0]}"))
            return
        }

        val channel = try {
            Channel.valueOf(arguments[1].uppercase())
        } catch (e: IllegalArgumentException) {
            sender.execute(SendMessage(chatId, "Неверный канал: ${arguments[1]}"))
            return
        }

        val text = arguments.drop(2).joinToString(" ")

        val template = Template(
            eventType = eventType,
            channel = channel,
            text = text
        )

        templateService.save(template)
        sender.execute(SendMessage(chatId, "✅ Шаблон успешно добавлен."))
    }
}
