package org.example.storage.repository

import org.example.storage.model.Group
import org.example.storage.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface GroupRepository : JpaRepository<Group, UUID> {

    fun findByName(name: String): Group?

    fun findByNameAndChatId(name: String, chatId: String?): Group?

    fun findByChatId(chatId: String): Group?

    fun findAllByName(name: String): List<Group>

    fun findGlobalByName(name: String): Group?

    @Query(
        """
        SELECT g FROM Group g
        WHERE g.name = :name AND g.chatId IS NULL AND g.isPrivate = true AND :user MEMBER OF g.allowedUsers
        """
    )
    fun findAccessiblePrivateGroupByName(
        @Param("name") name: String,
        @Param("user") user: User
    ): Group?

    @Query(
        """
        SELECT DISTINCT g FROM Group g
        LEFT JOIN FETCH g.allowedUsers
        LEFT JOIN FETCH g.owner
        LEFT JOIN FETCH g.notifiers
        WHERE g.name = :name AND COALESCE(g.chatId, '') = COALESCE(:chatId, '')
        """
    )
    fun findByNameWithUsers(
        @Param("name") name: String,
        @Param("chatId") chatId: String?
    ): Group?

    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.allowedUsers LEFT JOIN FETCH g.owner LEFT JOIN FETCH g.notifiers")
    fun findAllWithUsers(): List<Group>

    @Query(
        """
    SELECT DISTINCT g FROM Group g
    LEFT JOIN FETCH g.allowedUsers
    LEFT JOIN FETCH g.owner
    WHERE g.name = :name
    """
    )
    fun findAllByNameWithUsers(@Param("name") name: String): List<Group>

}
