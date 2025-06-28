package org.example.storage.repository

import org.example.storage.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {
    fun findByTelegramId(telegramId: Long): User?
}