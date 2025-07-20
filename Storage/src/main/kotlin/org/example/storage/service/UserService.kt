package org.example.storage.service

import org.example.storage.model.*
import org.example.storage.repository.*
import org.springframework.stereotype.Service
import java.util.*
import org.telegram.telegrambots.meta.api.objects.User as TelegramUser

@Service
class UserService(private val userRepository: UserRepository) {
    fun findById(id: UUID): User? = userRepository.findById(id).orElse(null)
    fun findByTelegramId(telegramId: Long): User? = userRepository.findByTelegramId(telegramId)
    fun save(user: User): User = userRepository.save(user)
    fun findAll(): List<User> = userRepository.findAll()
    fun delete(id: UUID) = userRepository.deleteById(id)

    fun resolveUser(tgUser: org.telegram.telegrambots.meta.api.objects.User): User {
        return userRepository.findByTelegramId(tgUser.id)
            ?: userRepository.save(
                User(
                    telegramId = tgUser.id,
                    chatId = tgUser.id.toString(), // Telegram chatId == telegram user id
                    username = tgUser.userName ?: "unknown"
                )
            )
    }

    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    fun create(telegramId: Long, chatId: String?, username: String?): User {
        val user = User(
            telegramId = telegramId,
            chatId = chatId ?: telegramId.toString(), // если chatId null — используем telegramId
            username = username ?: "unknown"
        )
        return userRepository.save(user)
    }



}

