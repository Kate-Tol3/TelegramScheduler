package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class ListGroupsCommand(
    private val groupService: GroupService,
    private val userService: UserService
) : BotCommand("list_groups", "Показать все доступные группы") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()
        val dbUser = userService.resolveUser(user)
        val allGroups = groupService.findAllWithUsers()

        println("📋 Отображение групп для пользователя ${dbUser.username} (id=${dbUser.telegramId})")
        println("🗣 Тип чата: ${chat.type}, chatId=$chatId")
        println("🔎 Всего групп в системе: ${allGroups.size}")

        val globalPublicGroups = allGroups.filter {
            it.chatId == null && !it.isPrivate
        }.sortedBy { it.name }

        val localGroups = allGroups.filter {
            it.chatId == chatId && (!it.isPrivate || it.owner?.id == dbUser.id || dbUser in it.allowedUsers)
        }.sortedBy { it.name }

        val privateGlobalGroups = allGroups.filter {
            it.chatId == null &&
                    it.isPrivate &&
                    it.allowedUsers.any { u -> u.id == dbUser.id }
        }.sortedBy { it.name }

        val privateLocalGroups = allGroups.filter {
            it.chatId != null &&
                    it.isPrivate &&
                    it.allowedUsers.any { u -> u.id == dbUser.id }
        }.sortedBy { it.name }


        println("🌐 Глобальные публичные: ${globalPublicGroups.map { it.name }}")
        println("📍 Локальные доступные: ${localGroups.map { it.name }}")
        println("🔒 Приватные глобальные: ${privateGlobalGroups.map { it.name }}")
        println("🔐 Приватные локальные (чужие чаты): ${privateLocalGroups.map { it.name }}")

        if (
            globalPublicGroups.isEmpty() &&
            localGroups.isEmpty() &&
            privateGlobalGroups.isEmpty() &&
            privateLocalGroups.isEmpty()
        ) {
            sender.execute(SendMessage(chatId, "❗️Пока нет доступных вам групп."))
            return
        }

        val builder = StringBuilder()

        if (globalPublicGroups.isNotEmpty()) {
            builder.appendLine("🌐 *Глобальные группы:*")
            globalPublicGroups.forEach {
                builder.appendLine("- ${escape(it.name)}")
            }
        }

        if (localGroups.isNotEmpty()) {
            builder.appendLine("\n📍 *Локальные группы:*")
            localGroups.forEach {
                val label = buildString {
                    append(it.name)
                    if (it.owner?.id == dbUser.id) append(" (ваша)")
                    if (it.isPrivate && it.owner?.id != dbUser.id) append(" [приватная]")
                }
                builder.appendLine("- ${escape(label)}")
            }
        }

        if (privateGlobalGroups.isNotEmpty()) {
            builder.appendLine("\n🔒 *Приватные глобальные группы:*")
            privateGlobalGroups.forEach {
                builder.appendLine("- ${escape(it.name)}")
            }
        }

        if (privateLocalGroups.isNotEmpty()) {
            builder.appendLine("\n🔐 *Приватные локальные группы:*")
            privateLocalGroups.forEach {
                builder.appendLine("- ${escape(it.name)}")
            }
        }

        sender.execute(SendMessage(chatId, builder.toString().trim()).apply {
            parseMode = "Markdown"
        })
    }

    private fun escape(text: String): String {
        val charsToEscape = listOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
        return charsToEscape.fold(text) { acc, c -> acc.replace(c.toString(), "\\$c") }
    }
}
