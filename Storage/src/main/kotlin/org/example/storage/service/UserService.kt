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

    fun resolveUser(telegramUser: TelegramUser): User {
        return findByTelegramId(telegramUser.id.toLong())
            ?: save(
                User(
                    telegramId = telegramUser.id.toLong(),
                    username = telegramUser.userName ?: "unknown"
                )
            )
    }
}

