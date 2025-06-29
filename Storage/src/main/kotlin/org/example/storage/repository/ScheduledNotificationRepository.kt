package org.example.storage.repository

import org.example.storage.model.ScheduledNotification
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ScheduledNotificationRepository : JpaRepository<ScheduledNotification, UUID>
