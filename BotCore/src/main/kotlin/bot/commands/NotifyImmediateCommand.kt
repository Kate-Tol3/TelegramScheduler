package org.example.bot.commands

import org.example.bot.sender.NotificationSender
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

class NotifyImmediateCommand(
    private val eventService: EventService,
    private val templateService: TemplateService,
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService,
    private val notificationSender: NotificationSender
) : BotCommand("notify_immediate", "Мгновенное уведомление") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, args: Array<out String>) {
        val chatId = chat.id.toString()

        if (args.size < 7) {
            sender.execute(SendMessage(chatId, """
                ❌ Неверный формат. Пример:
                /notify_immediate <CALL|MR|RELEASE> <ссылка> <место> <время> <описание> <chat|private> <группа>
            """.trimIndent()))
            return
        }

        // 🔹 Событие
        val eventType = try {
            EventType.valueOf(args[0].uppercase())
        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "❌ Неверный тип события. CALL, MR, RELEASE"))
            return
        }

        // 🔹 Ссылка
        val link = args[1]

        // 🔹 Время начала события
        val timeRegex = Regex("""\d{1,2}:\d{2}""")
        val timeIndex = args.indexOfFirst { timeRegex.matches(it) }

        if (timeIndex == -1 || timeIndex < 3) {
            sender.execute(SendMessage(chatId, "❌ Неверный формат времени. Пример: 14:30"))
            return
        }

        // 🔹 Место и описание
        val place = args.slice(2 until timeIndex).joinToString(" ")
        val timeStr = args[timeIndex]

        // 🔹 Поиск позиции chat|private
        val targetIndex = args.indexOfLast { it.equals("chat", true) || it.equals("private", true) }
        if (targetIndex == -1 || targetIndex == args.lastIndex) {
            sender.execute(SendMessage(chatId, "❌ Укажите 'chat' или 'private', а затем — имя группы."))
            return
        }

        val description = args.slice(timeIndex + 1 until targetIndex).joinToString(" ")
        val target = args[targetIndex].lowercase()

        // 🔧 Группа: всё, что после chat/private
        val groupName = args.drop(targetIndex + 1).joinToString(" ").trim()

        // 🔹 Время события
        val eventTime = try {
            val (h, m) = timeStr.split(":").map { it.toInt() }
            LocalDateTime.now().withHour(h).withMinute(m)
        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "❌ Неверный формат времени. Пример: 14:30"))
            return
        }

        // 🔹 Получаем пользователя и группу
        val dbUser = userService.resolveUser(user)

        val group = groupService.findByName(groupName, chatId, dbUser) { targetChatId ->
            try {
                val admins = sender.execute(GetChatAdministrators(targetChatId))
                admins.any { it.user.id == user.id }
            } catch (e: Exception) {
                println("⚠️ Ошибка при проверке админства: ${e.message}")
                false
            }
        }

        if (group == null) {
            sender.execute(SendMessage(chatId, "❌ Группа '$groupName' не найдена или у вас нет доступа."))
            return
        }

        // 🔐 Проверка доступа к отправке
        val isOwner = group.owner?.id == dbUser.id
        val isNotifier = groupService.isNotifier(group, dbUser)
        if (group.isPrivate && !isOwner && !isNotifier) {
            sender.execute(SendMessage(chatId, "❌ Только владелец или назначенные отправители могут отправлять уведомления в приватную группу '${group.name}'."))
            return
        }

        // 🔎 Проверка подписки
        val subscribers = subscriptionService.findUsersByGroup(group)
        val subscription = subscriptionService.findByUserAndGroup(dbUser, group)
        if (subscription == null) {
            sender.execute(SendMessage(chatId, "❌ Вы не подписаны на группу '${group.name}'"))
            return
        }

        // 📋 Получаем шаблон
        val template = templateService.findByEventType(eventType)
        if (template == null) {
            sender.execute(SendMessage(chatId, "❌ Шаблон для события '$eventType' не найден."))
            return
        }

        // 🛠 Формируем уведомление
        val payload = mapOf(
            "link" to link,
            "place" to place,
            "time" to eventTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            "description" to description
        )

        val event = eventService.createEvent(eventType, payload)
        val message = notificationSender.applyTemplate(template, payload)

        // 📤 Отправка
        when (target) {
            "chat" -> {
                val groupChatId = group.chatId?.toLongOrNull()
                if (groupChatId == null) {
                    sender.execute(SendMessage(chatId, "❌ У группы '${group.name}' нет привязанного чата."))
                    return
                }
                notificationSender.sendToGroups(sender, listOf(groupChatId), message)
                sender.execute(SendMessage(chatId, "✅ Уведомление отправлено в чат группы '${group.name}'"))
            }

            "private" -> {
                if (subscribers.isEmpty()) {
                    sender.execute(SendMessage(chatId, "⚠️ В группе '${group.name}' нет подписчиков."))
                    return
                }
                notificationSender.sendToUsers(sender, subscribers, message)
                sender.execute(SendMessage(chatId, "✅ Уведомление отправлено в ЛС подписчикам группы '${group.name}'"))
            }

            else -> {
                sender.execute(SendMessage(chatId, "❌ Укажите 'chat' или 'private' в параметрах."))
            }
        }
    }
}
