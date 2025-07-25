package org.example.restapi.controller

import org.example.restapi.dto.CreateTemplateRequest
import org.example.restapi.dto.TemplateDto
import org.example.restapi.mapper.toDto
import org.example.storage.model.Channel
import org.example.storage.model.EventType
import org.example.storage.model.Template
import org.example.storage.service.TemplateService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/templates")
class TemplateController(
    private val templateService: TemplateService
) {

    //GET/api/templates
    @GetMapping
    fun getAll(): List<TemplateDto> {
        return templateService.findAll().map { it.toDto() }
    }

    //POST/api/templates
    @PostMapping
    fun create(@RequestBody request: CreateTemplateRequest): TemplateDto {
        val template = Template(
            eventType = EventType.valueOf(request.eventType.uppercase()),
            channel = Channel.valueOf(request.channel.uppercase()),
            text = request.text
        )
        return templateService.save(template).toDto()
    }
}
