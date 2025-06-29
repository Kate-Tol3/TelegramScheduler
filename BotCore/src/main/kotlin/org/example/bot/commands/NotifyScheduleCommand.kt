package org.example.bot.commands

import org.example.storage.model.EventType
import org.example.storage.service.EventService
import org.example.storage.service.ScheduledNotificationService
import org.example.storage.service.TemplateService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.LocalDateTime

class NotifyScheduleCommand(
    private val eventService: EventService,
    private val templateService: TemplateService,
    private val scheduledNotificationService: ScheduledNotificationService
) : BotCommand("notify_schedule", "Запланировать уведомление") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 8) {
            sender.execute(
                SendMessage(
                    chatId,
                    "Использование:\n/notify_schedule <CALL|MR|RELEASE> <link> <place> <time> <description> <eventTime:yyyy-MM-ddTHH:mm> <repeatCount> <repeatIntervalMinutes>"
                )
            )
            return
        }

        try {
            val eventType = EventType.valueOf(arguments[0].uppercase())
            val link = arguments[1]
            val place = arguments[2]
            val time = arguments[3]

            val eventTimeIndex = arguments.size - 3
            val eventTime = LocalDateTime.parse(arguments[eventTimeIndex])
            val repeatCount = arguments[eventTimeIndex + 1].toInt()
            val repeatInterval = arguments[eventTimeIndex + 2].toInt()

            val description = arguments.slice(4 until eventTimeIndex).joinToString(" ")

            val event = eventService.createEvent(
                type = eventType,
                payload = mapOf(
                    "link" to link,
                    "place" to place,
                    "time" to time,
                    "description" to description
                )
            )

            val template = templateService.findByEventType(eventType)
            if (template == null) {
                sender.execute(SendMessage(chatId, "Нет шаблона для события $eventType"))
                return
            }

            scheduledNotificationService.create(
                template = template,
                eventTime = eventTime,
                repeatCount = repeatCount,
                repeatIntervalMinutes = repeatInterval,
                event = event
            )

            sender.execute(SendMessage(chatId, "Уведомление запланировано на $eventTime"))

        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "Ошибка: ${e.message}"))
        }
    }
}
