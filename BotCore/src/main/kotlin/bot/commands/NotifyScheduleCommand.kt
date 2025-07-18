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
    private val groupService: GroupService,
    private val userService: UserService,
    private val subscriptionService: SubscriptionService,
) : BotCommand("notify_schedule", "Запланировать уведомление") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 7) {
            sender.execute(SendMessage(chatId, """
                ⚠️ Недостаточно аргументов.
                Формат:
                /notify_schedule <CALL|MR|RELEASE> <ссылка> <время начала> <место...> <дата> <время> <описание...> [повторы в ЛС] [повторы в чат] [интервал] [группа]
            """.trimIndent()))
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

            val argsList = arguments.toList()
            val place = argsList.subList(3, dateIndex).joinToString(" ").ifBlank { "Место не указано" }

            val tail = arguments.drop(timeIndex + 1).toMutableList()

            // Предполагаем, что если есть хотя бы 4 элемента — могут быть повторы и группа
            var repeatUsers: Int? = null
            var repeatGroups: Int? = null
            var interval: Int? = null
            var groupName: String? = null

            if (tail.size >= 4 && tail.takeLast(4).dropLast(1).all { it.matches(Regex("\\d+")) }) {
                repeatUsers = tail.removeAt(tail.lastIndex - 3).toIntOrNull()
                repeatGroups = tail.removeAt(tail.lastIndex - 2).toIntOrNull()
                interval = tail.removeAt(tail.lastIndex - 1).toIntOrNull()
                groupName = tail.removeLast()
            }

            val description = tail.joinToString(" ").ifBlank { "Без описания" }

            val template = templateService.findByEventType(eventType)
            if (template == null) {
                sender.execute(SendMessage(chatId, "⚠️ Нет шаблона для типа события $eventType"))
                return
            }

            val dbUser = userService.resolveUser(user)

            val group = if (groupName != null) {
                groupService.findByName(groupName, chatId)
                    ?: groupService.findByName(groupName, null)
                    ?: run {
                        if (chat.isUserChat) {
                            val candidates = groupService.findAllByName(groupName)
                            for (candidate in candidates) {
                                val admins = candidate.chatId?.let {
                                    try {
                                        sender.execute(GetChatAdministrators(it))
                                    } catch (_: Exception) { null }
                                }
                                if (admins?.any { it.user.id == user.id } == true) {
                                    return@run candidate
                                }
                            }
                        }
                        sender.execute(SendMessage(chatId, "❌ Группа '$groupName' не найдена или вы не админ"))
                        return
                    }
            } else null

            if (group != null) {
                val isSenderSubscribed = subscriptionService.findUsersByGroup(group)
                    .any { it.telegramId == user.id }
                if (!isSenderSubscribed) {
                    sender.execute(SendMessage(chatId, "❌ Вы не подписаны на группу '${group.name}'. Уведомление не создано."))
                    return
                }
                if (repeatUsers == null || repeatGroups == null || interval == null) {
                    sender.execute(SendMessage(chatId, "⚠️ Укажите повторы и интервал при отправке в группу."))
                    return
                }
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

            scheduledNotificationService.create(
                template = template,
                eventTime = eventTime,
                repeatIntervalMinutes = interval ?: 0,
                repeatCountUsers = repeatUsers ?: 1,
                repeatCountGroups = repeatGroups ?: 0,
                event = event,
                group = group,
                users = if (group == null) setOf(dbUser) else emptySet()
            )

            val formattedTime = eventTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            sender.execute(SendMessage(chatId, "✅ Уведомление запланировано на $formattedTime"))

        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "❌ Ошибка: ${e.message}"))
        }
    }
}
