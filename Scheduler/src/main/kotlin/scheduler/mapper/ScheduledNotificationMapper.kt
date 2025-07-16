package org.example.scheduler.mapper

import org.example.messaging.dto.NotificationMessage
import org.example.storage.model.ScheduledNotification

fun ScheduledNotification.toMessage(): NotificationMessage {
    val group = this.targetGroups.firstOrNull()
    val groupName = group?.name ?: "all"

    val fallbackChatId = this.event.payload["originChatId"]
    val resolvedChatId = group?.chatId ?: fallbackChatId

    val text = applyTemplate(this.template.text, this.event.payload)

    return NotificationMessage(
        type = this.event.type.name,
        text = text,
        groupName = groupName,
        chatId = resolvedChatId,
        sendToGroup = this.repeatCountGroups > 0,
        sendToUsers = this.repeatCountUsers > 0
    )
}





private fun applyTemplate(template: String, payload: Map<String, String>): String {
    var result = template
    payload.forEach { (key, value) ->
        result = result.replace("{$key}", value)
    }
    return result
}
