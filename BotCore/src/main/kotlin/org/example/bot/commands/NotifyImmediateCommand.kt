package org.example.bot.commands

import org.example.bot.sender.NotificationSender
import org.example.storage.model.EventType
import org.example.storage.service.EventService
import org.example.storage.service.TemplateService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class NotifyImmediateCommand(
    private val eventService: EventService,
    private val templateService: TemplateService,
    private val userService: UserService,
    private val notificationSender: NotificationSender
) : BotCommand("notify_immediate", "Мгновенное уведомление") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 5) {
            sender.execute(SendMessage(chatId, "Использование:\n/notify_immediate <CALL|MR|RELEASE> <link> <place> <time> <description>"))
            return
        }

        val eventType = try {
            EventType.valueOf(arguments[0].uppercase())
        } catch (e: IllegalArgumentException) {
            sender.execute(SendMessage(chatId, "Недопустимый тип события. Используйте CALL, MR или RELEASE."))
            return
        }

        val (link, place, time) = arguments.slice(1..3)
        val description = arguments.drop(4).joinToString(" ")

        val placeholders = mapOf(
            "link" to link,
            "place" to place,
            "time" to time,
            "description" to description
        )

        eventService.createEvent(eventType, placeholders)

        val template = templateService.findByEventType(eventType)
        if (template == null) {
            sender.execute(SendMessage(chatId, "Нет шаблона для события $eventType"))
            return
        }

        val messageText = notificationSender.applyTemplate(template, placeholders)

        // Получим всех пользователей
        val users = userService.findAll()
        val groupChats = userService.findAll() // предполагается, что потом сюда попадут chatId групп

        notificationSender.sendToUsers(sender, users, messageText)
        // notificationSender.sendToGroups(sender, groupChatIds, messageText) — позже при добавлении чатов

        sender.execute(SendMessage(chatId, "Уведомление отправлено."))
    }
}
