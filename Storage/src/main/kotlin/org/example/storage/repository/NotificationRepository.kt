package org.example.storage.repository

import org.example.storage.model.Notification
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface NotificationRepository : JpaRepository<Notification, UUID>
