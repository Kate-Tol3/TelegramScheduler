package org.example.bot.commands

import org.example.storage.service.*
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class SubscribeAllCommand(
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService
) : BotCommand("subscribe_all", "Подписать всех участников чата") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        // Проверяем, что вызывающий — админ
        val admins = sender.execute(GetChatAdministrators(chatId)).map { it.user.id }
        if (user.id !in admins) {
            sender.execute(SendMessage(chatId, "⛔ Только администратор чата может выполнить эту команду."))
            return
        }

        val group = groupService.findByName(chat.title, chatId)
        if (group == null) {
            sender.execute(SendMessage(chatId, "⚠️ Группа для этого чата не найдена."))
            return
        }

        try {
            val members = sender.execute(org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount(chatId))
            var count = 0

            for (adminId in admins) {
                val member = sender.execute(GetChatMember(chatId, adminId))
                val tgUser = member.user
                if (!tgUser.isBot) {
                    val userModel = userService.resolveUser(tgUser)
                    val subscribed = subscriptionService.subscribe(userModel, group)
                    if (subscribed) count++
                }
            }

            sender.execute(SendMessage(chatId, "✅ Подписано $count пользователей."))
        } catch (e: Exception) {
            sender.execute(SendMessage(chatId, "❌ Ошибка при подписке: ${e.message}"))
        }
    }
}
