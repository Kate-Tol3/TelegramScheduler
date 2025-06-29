package org.example.storage.service


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.example.storage.model.Event
import org.example.storage.model.EventType
import org.example.storage.repository.EventRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*



private val objectMapper = jacksonObjectMapper()

@Service
class EventService(private val eventRepository: EventRepository) {
    fun findById(id: UUID): Event? = eventRepository.findById(id).orElse(null)
    fun findAll(): List<Event> = eventRepository.findAll()
    fun save(event: Event): Event = eventRepository.save(event)
    fun delete(id: UUID) = eventRepository.deleteById(id)

    fun createEvent(type: EventType, payload: Map<String, String>): Event {
        val event = Event(
            id = UUID.randomUUID(),
            type = type,
            payload = payload,
            createdAt = LocalDateTime.now()
        )
        return eventRepository.save(event)
    }

}

