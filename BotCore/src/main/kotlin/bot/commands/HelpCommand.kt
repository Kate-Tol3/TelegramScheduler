package org.example.bot.commands

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class HelpCommand : BotCommand("help", "Список всех доступных команд") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()
        val isPrivate = chat.isUserChat
        val isGroup = chat.isGroupChat || chat.isSuperGroupChat

        var isAdminInGroup = false
        if (isGroup) {
            try {
                val admins = sender.execute(GetChatAdministrators(chatId))
                isAdminInGroup = admins.any { it.user.id == user.id }
            } catch (_: Exception) { }
        }

        val helpText = buildString {
            appendLine("📖 *Доступные команды:*")

            appendLine("\n🟢 *Общие:*")
            appendLine("• `/start` — Начать работу с ботом")
            appendLine("• `/help` — Показать это меню")

            appendLine("\n🔔 *Подписки:*")
            appendLine("• `/subscribe <группа>` — Подписаться на уведомления")
            appendLine("• `/unsubscribe <группа>` — Отписаться от уведомлений")
            appendLine("• `/my_subscriptions` — Мои подписки")
            appendLine("• `/list_groups` — Список доступных групп")
            if (isGroup && isAdminInGroup) {
                appendLine("• `/subscribe_all` — Подписать всех админов этого чата")
            } else if (isPrivate) {
                appendLine("• `/subscribe_all <ссылка>` — Отправить кнопку подписки в чат")
            }

            appendLine("\n👥 *Управление группами:*")
            if (isPrivate) {
                appendLine("• `/create_group <имя>; <описание>` — Создать свою приватную группу")
                appendLine("• `/delete_group <имя>` — Удалить свою группу")
            }
            appendLine("• `/grant_access <группа> @user` — Дать доступ к группе")
            appendLine("• `/revoke_access <группа> @user` — Забрать доступ")
            appendLine("• `/allowed_users <группа>` — Кто имеет доступ")
            appendLine("• `/grant_notify_rights <группа> @user` — Разрешить отправку уведомлений")
            appendLine("• `/revoke_notify_rights <группа> @user` — Запретить отправку уведомлений")
            appendLine("• `/notifiers <группа>` — Кто может отправлять уведомления")


            appendLine("\n🧩 *Шаблоны:*")
            if (isPrivate) {
                appendLine("• `/list_templates` — Показать шаблоны уведомлений")
            }

            appendLine("\n🚀 *Уведомления:*")
            appendLine("• `/notify_immediate <тип> <ссылка> <место> <время> <описание> <private|chat> <группа>`")
            appendLine("• `/notify_schedule <CALL|MR|RELEASE> <ссылка> <время начала> <место...> <дата> <время> <описание...>; <доставок в ЛС> <доставок в группу> <интервал (минут)> [группа]`")
        }

        sender.execute(SendMessage(chatId, helpText).apply {
            parseMode = "Markdown"
        })
    }
}
