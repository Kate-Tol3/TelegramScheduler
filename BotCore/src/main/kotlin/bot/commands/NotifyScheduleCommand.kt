package org.example.bot.commands

import org.example.storage.model.EventType
import org.example.storage.service.*
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
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

        if (arguments.size < 11) {
            sender.execute(
                SendMessage(
                    chatId,
                    """
                    ⚠️ Недостаточно аргументов. 
                    Формат:
                    /notify_schedule <CALL|MR|RELEASE> <ссылка> <время начала> <место...> <дата> <время> <описание...> <повторы в ЛС> <повторы в группу> <интервал> [группа]
                    
                    Пример:
                    /notify_schedule CALL https://zoom.us 14:00 Zoom Кабинет 402 17.07.2025 13:45 Архитектура обсуждение 2 1 5 команда-разработка
                    """.trimIndent()
                )
            )
            return
        }

        try {
            val eventType = EventType.valueOf(arguments[0].uppercase())
            val link = arguments[1]
            val startTime = arguments[2]

            val dateIndex = arguments.indexOfFirst { it.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}")) }
            val timeIndex = dateIndex + 1

            if (dateIndex == -1 || timeIndex >= arguments.size || !arguments[timeIndex].matches(Regex("\\d{2}:\\d{2}"))) {
                sender.execute(SendMessage(chatId, "⚠️ Неверный формат даты и времени уведомления."))
                return
            }

            val dateStr = arguments[dateIndex]
            val timeStr = arguments[timeIndex]
            val eventTime = try {
                LocalDateTime.parse("$dateStr $timeStr", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            } catch (e: DateTimeParseException) {
                sender.execute(SendMessage(chatId, "⚠️ Ошибка разбора даты: $dateStr $timeStr"))
                return
            }

            val tail = arguments.drop(timeIndex + 1).toMutableList()
            if (tail.size < 3) {
                sender.execute(SendMessage(chatId, "⚠️ Недостаточно аргументов после даты."))
                return
            }

            var groupName: String? = null
            if (!tail.last().matches(Regex("\\d+"))) {
                groupName = tail.removeLast()
            }

            val interval = tail.removeLast().toIntOrNull()
            val repeatGroups = tail.removeLast().toIntOrNull()
            val repeatUsers = tail.removeLast().toIntOrNull()

            if (interval == null || repeatGroups == null || repeatUsers == null) {
                sender.execute(SendMessage(chatId, "⚠️ Неверные числовые значения повторов или интервала."))
                return
            }

            val place = arguments.toList().subList(3, dateIndex).joinToString(" ").ifBlank { "Место не указано" }
            val description = tail.joinToString(" ").ifBlank { "Без описания" }

            val template = templateService.findByEventType(eventType)
            if (template == null) {
                sender.execute(SendMessage(chatId, "⚠️ Нет шаблона для типа события $eventType"))
                return
            }

            val event = eventService.createEvent(
                type = eventType,
                payload = mapOf(
                    "link" to link,
                    "place" to place,
                    "time" to startTime,
                    "description" to description,
                    "originChatId" to chatId
                )
            )

            val group = if (groupName != null) {
                groupService.findByName(groupName, chatId)
                    ?: groupService.findByName(groupName, null)
                    ?: run {
                        if (chat.isUserChat) {
                            val candidateGroups = groupService.findAllByName(groupName)
                            for (candidate in candidateGroups) {
                                val targetChatId = candidate.chatId
                                if (targetChatId != null) {
                                    try {
                                        val admins = sender.execute(GetChatAdministrators(targetChatId))
                                        val isAdmin = admins.any { it.user.id == user.id }
                                        if (isAdmin) {
                                            return@run candidate
                                        }
                                    } catch (e: Exception) {
                                        sender.execute(SendMessage(chatId, "⚠️ Не удалось проверить админа в '$targetChatId': ${e.message}"))
                                        return
                                    }
                                }
                            }
                        }

                        sender.execute(SendMessage(chatId, "❌ Группа '$groupName' не найдена или вы не являетесь её админом"))
                        return
                    }
            } else {
                groupService.createGroup(
                    name = "temp_${chatId.takeLast(8)}",
                    description = "Временная группа для $chatId",
                    chatId = chatId
                )
            }




            scheduledNotificationService.create(
                template = template,
                eventTime = eventTime,
                repeatIntervalMinutes = interval,
                repeatCountUsers = repeatUsers,
                repeatCountGroups = repeatGroups,
                event = event,
                group = group,
                users = emptySet()
            )

            val formattedTime = eventTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            sender.execute(SendMessage(chatId, "✅ Уведомление запланировано на $formattedTime"))

        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "❌ Ошибка: ${e.message}"))
        }
    }
}
