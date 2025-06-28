package org.example.storage.service

import org.example.storage.model.*
import org.example.storage.repository.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class NotificationService(private val notificationRepository: NotificationRepository) {
    fun save(notification: Notification): Notification = notificationRepository.save(notification)
    fun findById(id: UUID): Notification? = notificationRepository.findById(id).orElse(null)
    fun findAll(): List<Notification> = notificationRepository.findAll()
    fun delete(id: UUID) = notificationRepository.deleteById(id)
}