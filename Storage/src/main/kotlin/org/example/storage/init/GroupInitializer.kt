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
            val existing = groupService.findByNameWithUsers(name, null)
            if (existing == null) {
                groupService.createGroup(
                    name = name,
                    description = "Глобальная группа $name"
                )
                println("✅ Группа '$name' создана")
            } else {
                println("ℹ️ Группа '$name' уже существует")
            }
        }
    }
}
