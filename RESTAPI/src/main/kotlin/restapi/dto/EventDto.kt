// EventDto.kt
package org.example.restapi.dto

import java.util.*

data class EventDto(
    val id: UUID,
    val link: String,
    val place: String,
    val dateTime: String,
    val description: String,
    val eventType: String
)

data class CreateEventRequest(
    val link: String,
    val place: String,
    val dateTime: String,
    val description: String,
    val eventType: String
)
