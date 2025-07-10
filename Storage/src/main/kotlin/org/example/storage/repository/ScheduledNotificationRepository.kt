package org.example.storage.repository

import org.example.storage.model.ScheduledNotification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ScheduledNotificationRepository : JpaRepository<ScheduledNotification, UUID> {

    @Query("SELECT s FROM ScheduledNotification s JOIN FETCH s.targetUsers")
    fun findAllWithUsers(): List<ScheduledNotification>
}
