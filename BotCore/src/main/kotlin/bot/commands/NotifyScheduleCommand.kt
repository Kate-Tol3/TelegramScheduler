package org.example.bot.commands

import org.example.storage.model.EventType
import org.example.storage.service.*
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class NotifyScheduleCommand(
    private val eventService: EventService,
    private val templateService: TemplateService,
    private val scheduledNotificationService: ScheduledNotificationService,
    private val groupService: GroupService
) : BotCommand("notify_schedule", "Запланировать уведомление") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 6) {
            sender.execute(
                SendMessage(
                    chatId,
                    """
                    ❗ Пример использования:
                    /notify_schedule <CALL|MR|RELEASE> <ссылка> <время начала> <время уведомления: dd.MM.yyyy HH:mm> <место> <описание> [повторы интервал] [группа]
                    
                    Примеры:
                    /notify_schedule CALL https://zoom.us 14:00 17.07.2025 14:30 Zoom Обсудим архитектуру
                    /notify_schedule CALL https://zoom.us 14:00 17.07.2025 14:30 Zoom Архитектура 3 60
                    /notify_schedule CALL https://zoom.us 14:00 17.07.2025 14:30 Zoom Архитектура команда-разработка
                    /notify_schedule CALL https://zoom.us 14:00 17.07.2025 14:30 Zoom Архитектура 3 60 команда-разработка
                    """.trimIndent()
                )
            )
            return
        }

        try {
            val eventType = EventType.valueOf(arguments[0].uppercase())
            val link = arguments[1]
            val time = arguments[2]

            val eventDatePattern = Regex("\\d{2}\\.\\d{2}\\.\\d{4}")
            val eventTimePattern = Regex("\\d{2}:\\d{2}")
            val dateIndex = arguments.indexOfFirst { it.matches(eventDatePattern) }
            val timeIndex = arguments.indexOfFirst { it.matches(eventTimePattern) && arguments.indexOf(it) > dateIndex }

            if (dateIndex == -1 || timeIndex == -1 || timeIndex - dateIndex != 1) {
                sender.execute(SendMessage(chatId, "⚠️ Неверный формат даты и времени. Используй: dd.MM.yyyy HH:mm"))
                return
            }

            val eventTimeRaw = "${arguments[dateIndex]} ${arguments[timeIndex]}"
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            val eventTime = try {
                LocalDateTime.parse(eventTimeRaw, formatter)
            } catch (e: DateTimeParseException) {
                sender.execute(SendMessage(chatId, "⚠️ Ошибка разбора даты: $eventTimeRaw"))
                return
            }

            val place = arguments.getOrNull(timeIndex + 1) ?: "Место не указано"
            val tailArgs = arguments.drop(timeIndex + 2).toMutableList()

            var groupName: String? = null
            var repeatCount = 1
            var repeatInterval = 0

            // Обрабатываем необязательные параметры с конца
            if (tailArgs.size >= 3) {
                val rc = tailArgs[tailArgs.size - 3].toIntOrNull()
                val ri = tailArgs[tailArgs.size - 2].toIntOrNull()
                val gn = tailArgs.last()

                if (rc != null && ri != null) {
                    repeatCount = rc
                    repeatInterval = ri
                    groupName = gn
                    repeat(3) { tailArgs.removeLast() }
                }
            }

            if (tailArgs.size >= 2) {
                val rc = tailArgs[tailArgs.size - 2].toIntOrNull()
                val ri = tailArgs.last().toIntOrNull()
                if (rc != null && ri != null) {
                    repeatCount = rc
                    repeatInterval = ri
                    repeat(2) { tailArgs.removeLast() }
                }
            }

            if (tailArgs.size == 1 && tailArgs[0].toIntOrNull() == null) {
                groupName = tailArgs.removeLast()
            }

            val description = tailArgs.joinToString(" ").ifBlank { "Без описания" }

            val template = templateService.findByEventType(eventType)
            if (template == null) {
                sender.execute(SendMessage(chatId, "⚠️ Нет шаблона для события $eventType"))
                return
            }

            val event = eventService.createEvent(
                type = eventType,
                payload = mapOf(
                    "link" to link,
                    "place" to place,
                    "time" to time,
                    "description" to description,
                    "originChatId" to chatId
                )
            )

            val group = if (groupName != null) {
                groupService.findByName(groupName, chatId) ?: run {
                    sender.execute(SendMessage(chatId, "❌ Группа '$groupName' не найдена"))
                    return
                }
            } else {
                groupService.createGroup(
                    name = "temp_${chatId.takeLast(8)}",
                    description = "Группа по умолчанию для $chatId",
                    chatId = chatId
                )
            }

            scheduledNotificationService.create(
                template = template,
                eventTime = eventTime,
                repeatCount = repeatCount,
                repeatIntervalMinutes = repeatInterval,
                event = event,
                group = group,
                users = emptySet()
            )

            val responseText = if (groupName != null) {
                "✅ Уведомление запланировано на $eventTime для группы '${group.name}'"
            } else {
                "✅ Уведомление запланировано на $eventTime для этого чата"
            }

            sender.execute(SendMessage(chatId, responseText))

        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "❌ Ошибка: ${e.message}"))
        }
    }
}
