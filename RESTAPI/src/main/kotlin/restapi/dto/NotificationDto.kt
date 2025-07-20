// NotificationDto.kt
package org.example.restapi.dto

import java.util.*

data class ScheduleNotificationRequest(
    val eventId: UUID,
    val templateId: UUID,
    val eventTime: String,
    val repeatCountUsers: Int,
    val repeatCountGroups: Int,
    val repeatIntervalMinutes: Int,
    val targetUserIds: List<UUID> = emptyList(),
    val targetGroupIds: List<UUID> = emptyList()
)

data class SendImmediateNotificationRequest(
    val eventId: UUID,
    val templateId: UUID,
    val private: Boolean,
    val chat: Boolean,
    val groupName: String,
    val text: String // ← Добавляем это!
)

