package org.example.storage.repository

import org.example.storage.model.Schedule
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

interface ScheduleRepository : JpaRepository<Schedule, UUID> {
    fun findByScheduledTimeBefore(time: LocalDateTime): List<Schedule>

}