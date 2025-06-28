package org.example.storage.service

import org.example.storage.model.Schedule
import org.example.storage.repository.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class ScheduleService(private val scheduleRepository: ScheduleRepository) {
    fun findById(id: UUID): Schedule? = scheduleRepository.findById(id).orElse(null)
    fun save(schedule: Schedule): Schedule = scheduleRepository.save(schedule)
    fun findAll(): List<Schedule> = scheduleRepository.findAll()
    fun delete(id: UUID) = scheduleRepository.deleteById(id)
}
