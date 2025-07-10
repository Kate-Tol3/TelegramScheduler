package org.example.bot.commands

import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand

class HelpCommand : BotCommand("help", "Список всех доступных команд") {
    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val helpText = """
    /start — Начать работу с ботом
    /help — Список всех доступных команд
    /subscribe [group] — Подписка на уведомления по группе
    /unsubscribe [group] — Отписка от уведомлений по группе
    /create_group <group> ; <описание> — Создать собственную группу с описанием
    /list_groups — Список доступных групп
    /list_templates — Список доступных шаблонов
    /my_subscriptions — Показать мои текущие подписки
    /notify_immediate <CALL|MR|RELEASE> <link> <place> <time> <description> — Мгновенно отправить уведомление
    /notify_schedule <CALL | MR | RELEASE> <link> <place> <time> <description> <eventTime:yyyy-MM-ddTHH:mm> <repeatCount> <repeatIntervalMinutes> — Запланировать уведомление
""".trimIndent()

        sender.execute(SendMessage(chat.id.toString(), helpText))
    }
}


