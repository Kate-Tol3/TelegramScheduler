// ✅ Обновлённый NotifyImmediateCommand: отправка возможна только если отправитель подписан

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

    override fun execute(
        sender: AbsSender,
        user: User,
        chat: Chat,
        args: Array<out String>
    ) {
        val chatId = chat.id.toString()

        if (args.size < 8) {
            sender.execute(
                SendMessage(chatId, """
                    ❌ Неверный формат. Пример:
                    /notify_immediate <CALL|MR|RELEASE> <ссылка> <место> <время> <описание> <chat|private> <группа>
                """.trimIndent())
            )
            return
        }

        val eventType = try {
            EventType.valueOf(args[0].uppercase())
        } catch (e: IllegalArgumentException) {
            sender.execute(SendMessage(chatId, "❌ Неверный тип события. CALL, MR, RELEASE"))
            return
        }

        val link = args[1]
        val place = args[2]
        val timeStr = args[3]

        val eventTime = try {
            val parts = timeStr.split(":")
            val now = LocalDateTime.now()
            now.withHour(parts[0].toInt()).withMinute(parts[1].toInt())
        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "❌ Неверный формат времени. Пример: 14:30"))
            return
        }

        val target = args[args.size - 2].lowercase()
        val groupName = args.last()
        val description = args.slice(4..<args.size - 2).joinToString(" ")

        val dbUser = userService.resolveUser(user)

        val group = groupService.findByName(groupName, chatId)
            ?: groupService.findByName(groupName, null)
            ?: run {
                if (chat.isUserChat) {
                    val candidates = groupService.findAllByName(groupName)
                    for (candidate in candidates) {
                        val targetChatId = candidate.chatId
                        if (targetChatId != null) {
                            try {
                                val admins = sender.execute(GetChatAdministrators(targetChatId))
                                val isAdmin = admins.any { it.user.id == user.id }
                                if (isAdmin) return@run candidate
                            } catch (_: Exception) {}
                        }
                    }
                }
                sender.execute(SendMessage(chatId, "❌ Группа '$groupName' не найдена или вы не админ"))
                return
            }


        if (group == null) {
            sender.execute(SendMessage(chatId, "❌ Группа '$groupName' не найдена"))
            return
        }

        val isSenderSubscribed = subscriptionService.findUsersByGroup(group)
            .any { it.telegramId == user.id }

        if (!isSenderSubscribed) {
            sender.execute(SendMessage(chatId, "❌ Вы не подписаны на группу '$groupName'. Уведомление не отправлено."))
            return
        }

        val payload = mapOf(
            "link" to link,
            "place" to place,
            "time" to eventTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            "description" to description
        )

        val event = eventService.createEvent(eventType, payload)
        val template = templateService.findByEventType(eventType)

        if (template == null) {
            sender.execute(SendMessage(chatId, "❌ Не найден шаблон для события $eventType"))
            return
        }

        val message = notificationSender.applyTemplate(template, payload)

        when (target) {
            "chat" -> {
                val groupChatId = group.chatId?.toLongOrNull()
                if (groupChatId == null) {
                    sender.execute(SendMessage(chatId, "❌ У группы '$groupName' нет привязанного чата"))
                    return
                }
                notificationSender.sendToGroups(sender, listOf(groupChatId), message)
                sender.execute(SendMessage(chatId, "✅ Уведомление отправлено в чат группы '$groupName'"))
            }

            "private" -> {
                val users = subscriptionService.findUsersByGroup(group)
                if (users.isEmpty()) {
                    sender.execute(SendMessage(chatId, "⚠️ В группе '$groupName' нет подписчиков"))
                    return
                }
                notificationSender.sendToUsers(sender, users, message)
                sender.execute(SendMessage(chatId, "✅ Уведомление отправлено в ЛС подписчикам группы '$groupName'"))
            }

            else -> {
                sender.execute(SendMessage(chatId, "❌ Неверный параметр <chat|private>: '$target'"))
            }
        }
    }
}