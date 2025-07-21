package org.example.bot.commands

import org.example.storage.service.*
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class SubscribeAllCommand(
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService
) : BotCommand("subscribe_all", "Подписать всех участников чата или отправить кнопку подписки") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val isPrivate = chat.isUserChat
        val chatId = chat.id.toString()
        val targetChatId: String

        if (isPrivate) {
            // Вызов из лички — ожидаем ссылку на группу
            if (arguments.isEmpty()) {
                sender.execute(SendMessage(chatId, "❌ Укажите ссылку на группу. Пример:\n/subscribe_all https://t.me/groupname"))
                return
            }

            val link = arguments[0].trim()
            if (!link.startsWith("https://t.me/")) {
                sender.execute(SendMessage(chatId, "❌ Неверный формат ссылки. Ожидается https://t.me/имя_группы"))
                return
            }

            val suffix = link.removePrefix("https://t.me/")
            if (suffix.startsWith("+")) {
                sender.execute(SendMessage(chatId, "❗ Приватные группы не поддерживаются по ссылке.\nДобавьте бота в группу и вызовите эту команду *из самой группы*."))
                return
            }

            targetChatId = "@$suffix"
        } else {
            // Вызов из самого группового чата
            targetChatId = chatId
        }

        // Проверяем, что пользователь — админ этой группы
        val isAdmin = try {
            val admins = sender.execute(GetChatAdministrators(targetChatId))
            admins.any { it.user.id == user.id }
        } catch (e: Exception) {
            false
        }

        if (!isAdmin) {
            sender.execute(SendMessage(chatId, "⛔ Только администратор указанного чата может выполнить эту команду."))
            return
        }

        val group = groupService.findByChatId(targetChatId)
        if (group == null) {
            sender.execute(SendMessage(chatId, "⚠️ Группа с chatId = $targetChatId не найдена. Убедитесь, что бот уже был добавлен в неё."))
            return
        }

        if (!isPrivate) {
            // Вызов из группы — подписываем всех админов и отправляем кнопку
            try {
                val admins = sender.execute(GetChatAdministrators(chatId))
                    .map { it.user }
                    .filter { !it.isBot }

                var count = 0
                for (tgUser in admins) {
                    val userModel = userService.resolveUser(tgUser)
                    if (subscriptionService.subscribe(userModel, group)) count++
                }

               // sender.execute(SendMessage(chatId, "✅ Подписано $count администраторов чата '${group.name}'"))

                // 🔘 Отправляем кнопку для остальных участников
                val button = InlineKeyboardButton.builder()
                    .text("📥 Подписаться на группу ${group.name}")
                    .callbackData("subscribe_group:${group.id}")
                    .build()

                val keyboard = InlineKeyboardMarkup(listOf(listOf(button)))

                sender.execute(
                    SendMessage(chatId, "👥 Участники, чтобы подписаться на группу *${group.name}*, нажмите кнопку ниже.")
                        .apply {
                            enableMarkdown(true)
                            replyMarkup = keyboard
                        }
                )

            } catch (e: Exception) {
                sender.execute(SendMessage(chatId, "❌ Ошибка при подписке: ${e.message}"))
            }
        }

    }
}
