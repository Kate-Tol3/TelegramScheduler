package org.example.storage.service

import org.example.storage.model.*
import org.example.storage.repository.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupService(private val groupRepository: GroupRepository) {
    fun findById(id: UUID): Group? = groupRepository.findById(id).orElse(null)
    fun findByName(name: String): Group? = groupRepository.findByName(name)
    fun save(group: Group): Group = groupRepository.save(group)
    fun findAll(): List<Group> = groupRepository.findAll()
    fun delete(id: UUID) = groupRepository.deleteById(id)
}