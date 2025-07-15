package org.example.bot

import jakarta.annotation.PostConstruct
import org.example.bot.commands.*
import org.example.bot.sender.NotificationSender
import org.example.storage.repository.EventRepository
import org.example.storage.repository.ScheduledNotificationRepository
import org.example.storage.service.*
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
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
//    private val testScheduleCommand: TestScheduleCommand
) : TelegramLongPollingCommandBot() {

    override fun getBotUsername(): String = botProperties.username
    override fun getBotToken(): String = botProperties.token

    @PostConstruct
    fun registerCommands() {
        register(StartCommand())
        register(HelpCommand())
        register(ListTemplatesCommand(templateService))
        register(SubscribeCommand(userService, groupService, subscriptionService))
        register(UnsubscribeCommand(userService, groupService, subscriptionService))
        register(MySubscriptionsCommand(userService, subscriptionService))
        register(ListGroupsCommand(groupService))
        register(CreateGroupCommand(groupService))
        register(NotifyImmediateCommand(eventService, templateService,userService, notificationSender))
        register(NotifyScheduleCommand(eventService,templateService,scheduledNotificationService, groupService))
        register(AddTemplateCommand(templateService))
//        register(testScheduleCommand)
    }

    override fun processNonCommandUpdate(update: Update) {
        val message = update.message ?: return
        val text = message.text ?: return
        val chatId = message.chatId.toString()

        val reply = SendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд.")
        execute(reply)
    }
}


