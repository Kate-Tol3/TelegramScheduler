package org.example.restapi.mapper

import org.example.restapi.dto.TemplateDto
import org.example.storage.model.Template
import org.example.restapi.dto.CreateTemplateRequest
import org.example.storage.model.Channel
import org.example.storage.model.EventType

fun Template.toDto(): TemplateDto = TemplateDto(
    id = this.id!!,
    eventType = this.eventType.name,
    channel = this.channel.name,
    text = this.text
)

fun CreateTemplateRequest.toEntity(): Template = Template(
    eventType = EventType.valueOf(this.eventType),
    channel = Channel.valueOf(this.channel),
    text = this.text
)
