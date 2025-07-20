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

        val local = mutableListOf<String>()
        val global = mutableListOf<String>()
        val private = mutableListOf<String>()

        for (subscription in allSubscriptions) {
            val group = subscription.group
            val name = subscription.groupName

            when {
                // Приватные глобальные
                group.chatId == null && group.isPrivate -> private += name

                // Публичные глобальные
                group.chatId == null && !group.isPrivate -> global += name

                // Локальные группы
                group.chatId != null -> local += name
            }
        }

        if (local.isEmpty() && global.isEmpty() && private.isEmpty()) {
            sender.execute(SendMessage(chatId, "❗️Вы пока не подписаны ни на одну группу."))
            return
        }

        val builder = StringBuilder("*📋 Ваши подписки:*")

        if (local.isNotEmpty()) {
            builder.appendLine("\n\n📍 *Локальные группы:*")
            local.forEach { builder.appendLine("- ${escape(it)}") }
        }

        if (global.isNotEmpty()) {
            builder.appendLine("\n🌐 *Глобальные группы:*")
            global.forEach { builder.appendLine("- ${escape(it)}") }
        }

        if (private.isNotEmpty()) {
            builder.appendLine("\n🔒 *Приватные группы:*")
            private.forEach { builder.appendLine("- ${escape(it)}") }
        }

        sender.execute(SendMessage(chatId, builder.toString()).apply {
            parseMode = "Markdown"
        })
    }

    private fun escape(text: String): String {
        val charsToEscape = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        var result = text
        for (char in charsToEscape) {
            result = result.replace(char.toString(), "\\$char")
        }
        return result
    }
}
