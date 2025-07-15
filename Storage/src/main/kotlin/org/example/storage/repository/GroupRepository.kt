package org.example.storage.repository

import org.example.storage.model.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface GroupRepository : JpaRepository<Group, UUID> {
    fun findByName(name: String): Group?

    fun findByNameAndChatId(name: String, chatId: String?): Group?

    @Query("SELECT g FROM Group g WHERE g.name = :name AND g.chatId IS NULL")
    fun findGlobalByName(@Param("name") name: String): Group?
}
