// ✅ MySubscriptionsCommand: разделение подписок на глобальные и локальные

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
        val allSubscriptions = subscriptionService.findByUser(dbUser)

        val global = allSubscriptions.filter { it.group.chatId == null }
        val local = allSubscriptions.filter { it.group.chatId == chatId }

        if (global.isEmpty() && local.isEmpty()) {
            sender.execute(SendMessage(chatId, "Вы пока не подписаны ни на одну группу."))
            return
        }

        val builder = StringBuilder("Ваши подписки:")

        if (local.isNotEmpty()) {
            builder.append("\n\n📍 Локальные:")
            local.forEach { builder.append("\n- ${it.groupName}") }
        }

        if (global.isNotEmpty()) {
            builder.append("\n\n🌐 Глобальные:")
            global.forEach { builder.append("\n- ${it.groupName}") }
        }

        sender.execute(SendMessage(chatId, builder.toString()))
    }
}