package org.example.bot.commands

import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class MySubscriptionsCommand(
    private val userService: UserService,
    private val subscriptionService: SubscriptionService
) : BotCommand("my_subscriptions", "Показать мои подписки") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()
        val dbUser = userService.resolveUser(user)
        val subscriptions = subscriptionService.findByUser(dbUser)

        val messageText = if (subscriptions.isEmpty()) {
            "Вы пока не подписаны ни на одну группу."
        } else {
            "Вы подписаны на группы:\n" + subscriptions.joinToString("\n") { "- ${it.groupName}" }
        }

        sender.execute(SendMessage(chatId, messageText))
    }
}
