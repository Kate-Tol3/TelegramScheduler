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
    private val groupService: GroupService,
    private val userService: UserService,
    private val subscriptionService: SubscriptionService,
) : BotCommand("notify_schedule", "Запланировать уведомление") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.size < 7) {
            sender.execute(SendMessage(chatId, formatHelp("Недостаточно аргументов.")))
            return
        }

        val eventType = try {
            EventType.valueOf(arguments[0].uppercase())
        } catch (_: Exception) {
            sender.execute(SendMessage(chatId, formatHelp("❌ Неверный тип события. Используйте CALL, MR или RELEASE.")))
            return
        }

        val link = arguments[1]
        if (!link.startsWith("http")) {
            sender.execute(SendMessage(chatId, formatHelp("❌ Ссылка должна начинаться с http или https.")))
            return
        }

        val startTime = arguments[2]
        if (!startTime.matches(Regex("\\d{1,2}:\\d{2}"))) {
            sender.execute(SendMessage(chatId, formatHelp("❌ Время начала указано неверно. Пример: 15:00")))
            return
        }

        val dateIndex = arguments.indexOfFirst { it.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}")) }
        if (dateIndex == -1 || dateIndex + 1 >= arguments.size) {
            sender.execute(SendMessage(chatId, formatHelp("❌ Не найдена дата или время отправки уведомления.")))
            return
        }

        val dateStr = arguments[dateIndex]
        val timeStr = arguments[dateIndex + 1]
        val eventTime = try {
            LocalDateTime.parse("$dateStr $timeStr", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } catch (_: DateTimeParseException) {
            sender.execute(SendMessage(chatId, formatHelp("❌ Неверный формат даты или времени: $dateStr $timeStr")))
            return
        }

        val place = arguments.slice(3 until dateIndex).joinToString(" ").ifBlank { "Место не указано" }

        val tail = arguments.drop(dateIndex + 2).joinToString(" ")

        var description = "Без описания"
        var deliveriesToUsers = 1
        var deliveriesToGroup = 0
        var interval = 0
        var groupName: String? = null

        if (';' in tail) {
            val parts = tail.split(";", limit = 2)
            description = parts[0].trim().ifBlank { "Без описания" }

            val params = parts.getOrNull(1)?.trim()?.split(" ") ?: emptyList()
            if (params.size >= 3 &&
                params[0].matches(Regex("\\d+")) &&
                params[1].matches(Regex("\\d+")) &&
                params[2].matches(Regex("\\d+"))
            ) {
                deliveriesToUsers = params[0].toInt()
                deliveriesToGroup = params[1].toInt()
                interval = params[2].toInt()

                groupName = params.drop(3).joinToString(" ").trim().ifBlank { null }
            }
        } else {
            description = tail.ifBlank { "Без описания" }
        }

        val template = templateService.findByEventType(eventType)
        if (template == null) {
            sender.execute(SendMessage(chatId, "⚠️ Нет шаблона для типа события $eventType"))
            return
        }

        val dbUser = userService.resolveUser(user)
        val group = groupName?.let {
            groupService.findByName(it, chatId, dbUser) { targetChatId ->
                try {
                    val admins = sender.execute(org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators(targetChatId))
                    admins.any { it.user.id == user.id }
                } catch (e: Exception) {
                    println("⚠️ Ошибка при проверке админства: ${e.message}")
                    false
                }
            }
        }

        if (group != null) {
            val isSenderSubscribed = subscriptionService.findUsersByGroup(group)
                .any { it.telegramId == user.id }

            if (!isSenderSubscribed) {
                sender.execute(SendMessage(chatId, "❌ Вы не подписаны на группу \"${group.name}\". Уведомление не создано."))
                return
            }

            if (!groupService.isNotifier(group, dbUser)) {
                sender.execute(SendMessage(chatId, "❌ Только владелец или назначенные отправители могут отправлять уведомления в группу \"${group.name}\""))
                return
            }

            if (deliveriesToUsers <= 0 && deliveriesToGroup <= 0) {
                sender.execute(SendMessage(chatId, "⚠️ Укажите количество доставок (в ЛС или в группу), чтобы уведомление было доставлено."))
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

        try {
            scheduledNotificationService.create(
                template = template,
                eventTime = eventTime,
                repeatIntervalMinutes = interval,
                repeatCountUsers = deliveriesToUsers,
                repeatCountGroups = deliveriesToGroup,
                event = event,
                group = group,
                users = when {
                    group == null -> setOf(dbUser)
                    deliveriesToUsers > 0 -> subscriptionService.findUsersByGroup(group).toSet()
                    else -> emptySet()
                }
            )
        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "❌ Ошибка при создании уведомления: ${e.message}"))
            return
        }

        val formattedTime = eventTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        sender.execute(SendMessage(chatId, "✅ Уведомление запланировано на $formattedTime"))
    }

    private fun formatHelp(reason: String): String {
        return """
        $reason

📌 Формат команды:
/notify_schedule <CALL|MR|RELEASE> <ссылка> <время начала> <место...> <дата> <время> <описание...>; <доставок в ЛС> <доставок в группу> <интервал (минут)> [группа]

🔹 <доставок в ЛС> — сколько раз всего уведомление будет доставлено подписанным пользователям в личные сообщения.
🔹 <доставок в группу> — сколько раз всего уведомление будет отправлено в групповой чат.
🔹 <интервал> — пауза в минутах между доставками (общая для всех).

🧩 Пример:
/notify_schedule CALL https://zoom.us/j/123456789 15:00 Zoom 21.07.2025 15:00 Обсуждение проекта; 1 0 0 backend
        """.trimIndent()
    }
}