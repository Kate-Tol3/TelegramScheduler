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
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault


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
        register(StartCommand(groupService, subscriptionService, userService))
        register(HelpCommand())

        register(CreateGroupCommand(groupService, subscriptionService, userService))
        register(DeleteGroupCommand(groupService, subscriptionService, scheduledNotificationService, userService))
        register(ListGroupsCommand(groupService, userService))

        register(SubscribeCommand(userService, groupService, subscriptionService))
        register(UnsubscribeCommand(userService, groupService, subscriptionService))
        register(MySubscriptionsCommand(userService, subscriptionService))
        register(SubscribeAllCommand(userService, groupService, subscriptionService))
        register(MyChatsCommand(groupService))

        register(GrantAccessCommand(userService, groupService))
        register(RevokeAccessCommand(userService, groupService, subscriptionService))
        register(GrantNotifyRightsCommand(userService, groupService))
        register(RevokeNotifyRightsCommand(userService, groupService))
        register(AllowedUsersCommand(groupService, userService))
        register(NotifiersCommand(groupService, userService))

        register(ListTemplatesCommand(templateService))

        register(
            NotifyImmediateCommand(
                eventService, templateService, userService,
                groupService, subscriptionService, notificationSender
            )
        )

        register(
            NotifyScheduleCommand(
                eventService, templateService, scheduledNotificationService,
                groupService, userService, subscriptionService
            )
        )

        registerBotMenuCommands()
    }

    override fun processNonCommandUpdate(update: Update) {
        if (update.hasMyChatMember()) {
            handleMyChatMemberUpdate(update.myChatMember)
            return
        }

        if (update.hasCallbackQuery()) {
            processCallbackQuery(update)
            return
        }

        val message = update.message ?: return
        val chatId = message.chatId.toString()
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

            execute(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(callback.id)
                    .text(feedback)
                    .showAlert(false)
                    .build()
            )

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

    private fun handleMyChatMemberUpdate(memberUpdate: ChatMemberUpdated) {
        val chat = memberUpdate.chat
        val chatId = chat.id.toString()
        val newStatus = memberUpdate.newChatMember.status

        println("📥 my_chat_member: newStatus = $newStatus, chatId = $chatId")

        when (newStatus) {
            "left", "kicked" -> {
                val group = groupService.findByChatId(chatId)
                println("🔍 Найдена группа для удаления: ${group?.name}")

                if (group != null) {
                    println("🗑 Бот удалён из чата '${chat.title}', удаляем группу '${group.name}'")
                    scheduledNotificationService.deleteAllByGroup(group)
                    subscriptionService.deleteAllByGroup(group)
                    groupService.delete(group)
                }
            }

            "member", "administrator" -> {
                val title = chat.title ?: "группа-${chatId.takeLast(6)}"
                val existing = groupService.findByChatId(chatId)
                println("➕ Проверка: существует ли уже группа для $chatId → ${existing != null}")

                if (existing == null) {
                    val group = groupService.createGroup(
                        name = title,
                        description = "Группа Telegram $title",
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
                        println("✅ Группа '$title' создана и админы подписаны")
                    } catch (e: Exception) {
                        println("⚠️ Не удалось получить админов: ${e.message}")
                    }
                }
            }
        }
    }

    private fun registerBotMenuCommands() {
        val commands = listOf(
            BotCommand("start", "Начать работу с ботом"),
            BotCommand("help", "Список доступных команд"),
            BotCommand("subscribe", "Подписаться на группу"),
            BotCommand("unsubscribe", "Отписаться от группы"),
            BotCommand("list_groups", "Показать все группы"),
            BotCommand("my_subscriptions", "Мои подписки"),
            BotCommand("notify_immediate", "Срочное уведомление"),
            BotCommand("notify_schedule", "Запланировать уведомление"),
            BotCommand("create_group", "Создать группу"),
            BotCommand("delete_group", "Удалить свою группу"),
            BotCommand("subscribe_all", "Подписать всех участников"),
            BotCommand("grant_access", "Выдать доступ к группе"),
            BotCommand("revoke_access", "Отозвать доступ к группе"),
            BotCommand("grant_notify_rights", "Выдать право на уведомления"),
            BotCommand("revoke_notify_rights", "Отозвать право на уведомления"),
            BotCommand("my_chats", "Мои чаты"),
            BotCommand("list_templates", "Список шаблонов уведомлений")
        )

        execute(SetMyCommands(commands, BotCommandScopeDefault(), null))
    }

}
