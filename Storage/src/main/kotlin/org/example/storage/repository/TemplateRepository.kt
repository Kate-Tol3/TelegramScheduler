package org.example.storage.repository

import org.example.storage.model.EventType
import org.example.storage.model.Template
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TemplateRepository : JpaRepository<Template, UUID> {
    fun findFirstByEventType(eventType: EventType): Template?
}
