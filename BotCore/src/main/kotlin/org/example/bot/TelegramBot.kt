package org.example.bot

import jakarta.annotation.PostConstruct
import org.example.bot.commands.*
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
    private val subscriptionService: SubscriptionService
) : TelegramLongPollingCommandBot() {

    override fun getBotUsername(): String = botProperties.username
    override fun getBotToken(): String = botProperties.token

    @PostConstruct
    fun registerCommands() {
        register(StartCommand())
        register(HelpCommand())
        register(SubscribeCommand(userService, groupService, subscriptionService))
        register(UnsubscribeCommand(userService, groupService, subscriptionService))
        register(ListGroupsCommand())   // позже можно передать сервис
        register(CreateGroupCommand(groupService))
    }

    override fun processNonCommandUpdate(update: Update) {
        val message = update.message ?: return
        val text = message.text ?: return
        val chatId = message.chatId.toString()

        val reply = SendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд.")
        execute(reply)
    }
}
