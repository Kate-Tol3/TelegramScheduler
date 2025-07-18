package org.example.bot.commands

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class HelpCommand : BotCommand("help", "Список всех доступных команд") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val userId = user.id
        val chatId = chat.id.toString()

        val isPrivate = chat.isUserChat
        val isGroup = chat.isGroupChat || chat.isSuperGroupChat

        var isAdminInGroup = false
        if (isGroup) {
            try {
                val admins = sender.execute(GetChatAdministrators(chatId))
                isAdminInGroup = admins.any { it.user.id == userId }
            } catch (_: Exception) {}
        }

        val helpText = buildString {
            appendLine("📖 *Доступные команды:*\n")

            // 📌 Общие
            appendLine("▶️ `/start` — Начать работу с ботом")
            appendLine("🆘 `/help` — Показать это меню")

            // 🔔 Подписки
            appendLine("\n🔔 *Подписки:*")
            appendLine("➕ `/subscribe <group>` — Подписка на уведомления по группе")
            appendLine("➖ `/unsubscribe <group>` — Отписка от уведомлений по группе")
            appendLine("📋 `/my_subscriptions` — Показать мои текущие подписки")
            appendLine("🛡️ `/my_chats` — Показать чаты, где вы админ и бот присутствует")

            if (isGroup && isAdminInGroup) {
                appendLine("👥 `/subscribe_all` — Подписать всех участников этого чата (🛡️ только для админов)")
            } else if (isPrivate) {
                appendLine("👥 `/subscribe_all <ссылка на группу>` — Подписать участников группы (🛡️ только для админов)")
            }

            // 📦 Группы
            appendLine("\n📦 *Группы:*")
            if (isPrivate) {
                appendLine("➕ `/create_group <group> ; <описание>` — Создать свою группу")
                appendLine("❌ `/delete_group <group>` — Удалить свою группу")
            }
            appendLine("📃 `/list_groups` — Показать все доступные группы")

            // 🧩 Шаблоны
            if (isPrivate) {
                appendLine("\n🧩 *Шаблоны:*")
                appendLine("📃 `/list_templates` — Показать все шаблоны")
//              appendLine("➕ `/add_template <тип> ; <канал> ; <текст>` — Добавить шаблон") // отключено
            }

            // 🚀 Уведомления
            appendLine("\n🚀 *Уведомления:*")
            appendLine("⚡ `/notify_immediate <CALL|MR|RELEASE> <ссылка> <место> <время> <описание> <chat|private> <группа>` — Мгновенная отправка")
            appendLine("⏰ `/notify_schedule <CALL|MR|RELEASE> <ссылка> <место> <время> <описание> <дата> <время> <повторы в ЛС> <повторы в чат> <интервал> [группа]` — Запланировать уведомление")
        }

        sender.execute(
            SendMessage(chatId, helpText).apply {
                parseMode = "Markdown"
            }
        )
    }
}
