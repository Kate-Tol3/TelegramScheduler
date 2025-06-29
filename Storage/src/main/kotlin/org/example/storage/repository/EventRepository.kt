package org.example.storage.repository

import org.example.storage.model.Event
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EventRepository : JpaRepository<Event, UUID>
