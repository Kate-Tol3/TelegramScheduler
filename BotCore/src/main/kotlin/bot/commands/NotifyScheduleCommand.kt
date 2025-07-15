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
                    /notify_schedule <CALL|MR|RELEASE> <link> <время> <eventTime: dd.MM.yyyy HH:mm> <place> <description> [repeatCount] [repeatIntervalMinutes] [groupName]
                    
                    Пример:
                    /notify_schedule CALL https://zoom.us/ 14:00 17.07.2025 14:30 Zoom Обсудим архитектуру проекта 3 60 разработка
                    """.trimIndent()
                )
            )
            return
        }

        try {
            val eventType = EventType.valueOf(arguments[0].uppercase())
            val link = arguments[1]
            val time = arguments[2]

            // Ищем позицию eventTime по формату dd.MM.yyyy HH:mm
            val eventTimePattern = Regex("\\d{2}\\.\\d{2}\\.\\d{4}")
            val timePattern = Regex("\\d{2}:\\d{2}")
            val dateIndex = arguments.indexOfFirst { it.matches(eventTimePattern) }
            val timeIndex = arguments.indexOfFirst { it.matches(timePattern) && arguments.indexOf(it) > dateIndex }

            if (dateIndex == -1 || timeIndex == -1 || timeIndex - dateIndex != 1) {
                sender.execute(SendMessage(chatId, "⚠️ Неверный формат даты и времени. Используй формат: dd.MM.yyyy HH:mm"))
                return
            }

            val eventDateStr = arguments[dateIndex]
            val eventTimeStr = arguments[timeIndex]
            val eventTimeRaw = "$eventDateStr $eventTimeStr"

            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            val eventTime = try {
                LocalDateTime.parse(eventTimeRaw, formatter)
            } catch (e: DateTimeParseException) {
                sender.execute(SendMessage(chatId, "⚠️ Не удалось распарсить дату и время. Используй формат: dd.MM.yyyy HH:mm"))
                return
            }

            // Всё после времени — place и description (до возможных repeatCount и groupName)
            val baseArgsEnd = timeIndex + 1
            val remaining = arguments.drop(baseArgsEnd).toMutableList()

            // Попробуем вытащить необязательные параметры
            var repeatCount = 1
            var repeatInterval = 0
            var groupName: String? = null

            // Сначала groupName (если есть), потом repeatInterval, потом repeatCount
            if (remaining.size >= 3) {
                groupName = remaining.removeLast()
                repeatInterval = remaining.removeLast().toIntOrNull() ?: 0
                repeatCount = remaining.removeLast().toIntOrNull() ?: 1
            } else if (remaining.size == 2) {
                repeatInterval = remaining.removeLast().toIntOrNull() ?: 0
                repeatCount = remaining.removeLast().toIntOrNull() ?: 1
            } else if (remaining.size == 1) {
                repeatCount = remaining.removeLast().toIntOrNull() ?: 1
            }

            val place = remaining.firstOrNull() ?: "Место не указано"
            val description = remaining.drop(1).joinToString(" ")

            // Найти шаблон
            val template = templateService.findByEventType(eventType)
            if (template == null) {
                sender.execute(SendMessage(chatId, "⚠️ Нет шаблона для события $eventType"))
                return
            }

            // Создать событие
            val event = eventService.createEvent(
                type = eventType,
                payload = mapOf(
                    "link" to link,
                    "place" to place,
                    "time" to time,
                    "description" to description,
                    "originChatId" to chatId // 👈 вот это добавляем
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

            sender.execute(
                SendMessage(
                    chatId,
                    "✅ Уведомление запланировано на $eventTime для группы '${group.name}'"
                )
            )

        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "❌ Ошибка: ${e.message}"))
        }
    }
}
