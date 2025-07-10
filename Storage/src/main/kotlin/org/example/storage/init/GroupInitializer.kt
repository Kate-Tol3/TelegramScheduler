package org.example.storage.init

import jakarta.annotation.PostConstruct
import org.example.storage.service.GroupService
import org.springframework.stereotype.Component

@Component
class GroupInitializer(private val groupService: GroupService) {

    @PostConstruct
    fun initDefaultGroups() {
        val defaultNames = listOf("backend", "frontend", "devops", "design", "all")
        for (name in defaultNames) {
            if (groupService.findByName(name, null) == null) {
                groupService.createGroup(name)
            }
        }
    }
}
