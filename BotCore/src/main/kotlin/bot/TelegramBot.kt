package org.example.bot

import jakarta.annotation.PostConstruct
import org.example.bot.commands.*
import org.example.bot.sender.NotificationSender
import org.example.bot.service.BotContextService
import org.example.storage.service.*
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

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
        register(ListTemplatesCommand(templateService))
        register(SubscribeCommand(userService, groupService, subscriptionService))
        register(UnsubscribeCommand(userService, groupService, subscriptionService))
        register(MySubscriptionsCommand(userService, subscriptionService))
        register(ListGroupsCommand(groupService))
        register(CreateGroupCommand(groupService, subscriptionService, userService))
        register(NotifyImmediateCommand(eventService, templateService, userService, groupService, subscriptionService, notificationSender))
        register(NotifyScheduleCommand(eventService, templateService, scheduledNotificationService, groupService, userService, subscriptionService))
//        register(AddTemplateCommand(templateService))
        register(DeleteGroupCommand(groupService, subscriptionService))
        register(SubscribeAllCommand(userService, groupService, subscriptionService))
        register(MyChatsCommand(groupService))
    }

    override fun processNonCommandUpdate(update: Update) {
        val message = update.message ?: return
        val chat = message.chat
        val chatId = chat.id.toString()

        // 🟢 Групповой чат — создать группу и подписать админов
        if (chat.isGroupChat || chat.isSuperGroupChat) {
            val groupName = chat.title ?: "группа-${chatId.takeLast(6)}"
            val existing = groupService.findByName(groupName, chatId)

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

}
