package org.example.restapi.dto

import java.util.*

data class TemplateDto(
    val id: UUID,
    val eventType: String,
    val channel: String,
    val text: String
)

data class CreateTemplateRequest(
    val eventType: String,
    val channel: String,
    val text: String
)
