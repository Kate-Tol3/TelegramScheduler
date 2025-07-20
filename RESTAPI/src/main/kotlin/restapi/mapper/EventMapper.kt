package org.example.restapi.mapper

import org.example.restapi.dto.EventDto
import org.example.storage.model.Event

fun Event.toDto(): EventDto = EventDto(
    id = this.id!!,
    eventType = this.type.name, // исправлено!
    dateTime = this.createdAt.toString(),
    link = this.payload["link"] ?: "",
    place = this.payload["place"] ?: "",
    description = this.payload["description"] ?: ""
)
