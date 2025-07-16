package org.example.storage.service

import org.example.storage.model.Group
import org.example.storage.repository.GroupRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupService(private val groupRepository: GroupRepository) {

    fun findById(id: UUID): Group? = groupRepository.findById(id).orElse(null)

//    fun findByName(name: String): Group? = groupRepository.findByName(name)

    fun findByName(name: String, chatId: String?): Group? {
        return groupRepository.findByNameAndChatId(name, chatId)
            ?: groupRepository.findGlobalByName(name)
    }




    fun save(group: Group): Group = groupRepository.save(group)

    fun findAll(): List<Group> = groupRepository.findAll()


    fun delete(id: UUID) {
        groupRepository.deleteById(id)
    }

    fun delete(group: Group) {
        group.id?.let { delete(it) }
        // если id == null — просто ничего не делаем
    }

//    fun resolveGroupByName(name: String): Group {
//        return groupRepository.findByName(name)
//            ?: groupRepository.save(Group(name = name, description = "Группа $name"))
//    }

    fun createGroup(name: String, description: String = "", chatId: String? = null): Group {
        return groupRepository.save(Group(name = name, description = description, chatId = chatId))
    }

//    fun updateChatId(name: String, chatId: String): Group? {
//        val group = groupRepository.findByName(name)
//        return if (group != null) {
//            val updated = Group(
//                id = group.id,
//                name = group.name,
//                description = group.description,
//                chatId = chatId
//            )
//            groupRepository.save(updated)
//        } else null
//    }
}
