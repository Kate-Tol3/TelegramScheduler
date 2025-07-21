package org.example.bot.commands

import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class CreateGroupCommand(
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService,
    private val userService: UserService
) : BotCommand("create_group", "Создать новую группу") {

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<String>) {
        val chatId = chat.id.toString()

        if (arguments.isEmpty()) {
            sender.execute(
                SendMessage(chatId, "Укажите название и описание группы:\n\n`/create_group <группа> ; <описание>`").apply {
                    parseMode = "Markdown"
                }
            )
            return
        }

        val fullInput = arguments.joinToString(" ")
        val parts = fullInput.split(";", limit = 2)

        val groupName = parts[0].trim()
        val description = if (parts.size > 1) parts[1].trim() else ""

        if (groupName.isEmpty()) {
            sender.execute(SendMessage(chatId, "❗️Название группы не может быть пустым."))
            return
        }

        val dbUser = userService.resolveUser(user)

        // 🔍 Проверка: у пользователя уже есть группа с таким названием или доступ к такой
        val allGroupsWithSameName = groupService.findAllByName(groupName)
        val alreadyExistsForUser = allGroupsWithSameName.any { group ->
            group.owner?.id == dbUser.id || group.allowedUsers.any { it.id == dbUser.id }
        }

        if (alreadyExistsForUser) {
            sender.execute(
                SendMessage(chatId, "⚠️ У вас уже есть группа (или доступ к ней) с названием '${groupName}'. Переименуйте новую группу.")
            )
            return
        }

        // ✅ Создание новой группы
        val newGroup = groupService.createGroup(
            name = groupName,
            chatId = chatId,
            description = description,
            isPrivate = false,
            owner = dbUser,
            allowedUsers = emptySet()
        )

        subscriptionService.subscribe(dbUser, newGroup)

        println("✅ Создана локальная группа '${newGroup.name}' в чате $chatId владельцем ${dbUser.username}")
        sender.execute(
            SendMessage(chatId, """
                ✅ Группа '${newGroup.name}' успешно создана.
                ℹ️ Описание: $description
                👤 Владелец: ${dbUser.username}
            """.trimIndent())
        )
    }
}
