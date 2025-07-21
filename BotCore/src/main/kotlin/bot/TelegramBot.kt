package org.example.bot

import jakarta.annotation.PostConstruct
import org.example.bot.commands.*
import org.example.bot.sender.NotificationSender
import org.example.bot.service.BotContextService
import org.example.storage.service.*
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*

@Component
class TelegramBot(
    private val botProperties: BotProperties,
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService,
    private val templateService: TemplateService,
    private val eventService: EventService,
    private val scheduledNotificationService: ScheduledNotificationService,
    private val notificationSender: NotificationSender,
    private val botContextService: BotContextService,
) : TelegramLongPollingCommandBot() {

    override fun getBotUsername(): String = botProperties.username
    override fun getBotToken(): String = botProperties.token

    @PostConstruct
    fun registerCommands() {
        // ✅ Общие
        register(StartCommand(groupService, subscriptionService, userService))
        register(HelpCommand())

        // ✅ Работа с группами
        register(CreateGroupCommand(groupService, subscriptionService, userService))
        register(DeleteGroupCommand(groupService, subscriptionService, userService))
        register(ListGroupsCommand(groupService, userService))

        // ✅ Подписки
        register(SubscribeCommand(userService, groupService, subscriptionService))
        register(UnsubscribeCommand(userService, groupService, subscriptionService))
        register(MySubscriptionsCommand(userService, subscriptionService))
        register(SubscribeAllCommand(userService, groupService, subscriptionService)) // если используешь
        register(MyChatsCommand(groupService)) // если реализована

        // ✅ Доступ к приватным группам
        register(GrantAccessCommand(userService, groupService))
        register(RevokeAccessCommand(userService, groupService, subscriptionService))
        register(GrantNotifyRightsCommand(userService, groupService))
        register(RevokeNotifyRightsCommand(userService, groupService))
        register(AllowedUsersCommand(groupService, userService))

        // ✅ Шаблоны (если нужно)
        register(ListTemplatesCommand(templateService)) // если реализована

        // ✅ Уведомления
        register(
            NotifyImmediateCommand(
                eventService,
                templateService,
                userService,
                groupService,
                subscriptionService,
                notificationSender
            )
        )
        register(
            NotifyScheduleCommand(
                eventService,
                templateService,
                scheduledNotificationService,
                groupService,
                userService,
                subscriptionService
            )
        )
    }

    override fun processNonCommandUpdate(update: Update) {
        if (update.hasCallbackQuery()) {
            processCallbackQuery(update)
            return
        }

        val message = update.message ?: return
        val chat = message.chat
        val chatId = chat.id.toString()

        // 🟢 Если это групповой чат — регистрируем его
        if (chat.isGroupChat || chat.isSuperGroupChat) {
            val groupName = chat.title ?: "группа-${chatId.takeLast(6)}"
            val existing = groupService.findByName(groupName, chatId, null) // ← исправлено

            if (existing == null) {
                val group = groupService.createGroup(
                    name = groupName,
                    description = "Группа Telegram $groupName",
                    chatId = chatId
                )

                try {
                    val admins = execute(GetChatAdministrators(chatId))
                    for (admin in admins) {
                        val tgUser = admin.user
                        if (!tgUser.isBot) {
                            val user = userService.resolveUser(tgUser)
                            subscriptionService.subscribe(user, group)
                        }
                    }
                    println("✅ Группа '$groupName' создана и админы подписаны")
                } catch (e: Exception) {
                    println("⚠️ Не удалось получить админов: ${e.message}")
                }
            }
        }


        val text = message.text ?: return
        if (!text.startsWith("/")) {
            execute(SendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд."))
        }
    }

    private fun processCallbackQuery(update: Update) {
        val callback = update.callbackQuery ?: return
        val data = callback.data ?: return
        val user = callback.from
        val message = callback.message ?: return
        val chatId = message.chatId.toString()

        if (data.startsWith("subscribe_group:")) {
            val groupId = data.removePrefix("subscribe_group:")
            val group = try {
                groupService.findById(UUID.fromString(groupId))
            } catch (e: IllegalArgumentException) {
                execute(
                    AnswerCallbackQuery.builder()
                        .callbackQueryId(callback.id)
                        .text("❌ Некорректный идентификатор группы.")
                        .showAlert(true)
                        .build()
                )
                return
            }

            if (group == null) {
                execute(
                    AnswerCallbackQuery.builder()
                        .callbackQueryId(callback.id)
                        .text("❌ Группа не найдена.")
                        .showAlert(true)
                        .build()
                )
                return
            }

            val userModel = userService.resolveUser(user)
            val subscribed = subscriptionService.subscribe(userModel, group)

            val feedback = if (subscribed) {
                "✅ Вы подписались на группу '${group.name}'"
            } else {
                "⚠️ Вы уже подписаны на группу '${group.name}'"
            }

            // Всплывающее уведомление
            execute(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(callback.id)
                    .text(feedback)
                    .showAlert(false)
                    .build()
            )

            // Сообщение в чат, если это не личка
            if (!message.chat.isUserChat) {
                execute(SendMessage(chatId, feedback))
            }
        }
    }

    override fun processInvalidCommandUpdate(update: Update?) {
        val message = update?.message ?: return
        val chatId = message.chatId.toString()
        val command = message.text ?: return

        val reply = "❌ Неизвестная команда: $command\n\nИспользуйте /help для списка доступных команд."
        execute(SendMessage(chatId, reply))
    }


}
