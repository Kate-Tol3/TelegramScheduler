package org.example.storage.repository

import org.example.storage.model.Group
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface GroupRepository : JpaRepository<Group, UUID> {
    fun findByName(name: String): Group?
}
