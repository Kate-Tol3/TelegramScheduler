package org.example.storage.service

import org.example.storage.model.Group
import org.example.storage.model.User
import org.example.storage.repository.GroupRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupService(private val groupRepository: GroupRepository) {

    fun findById(id: UUID): Group? = groupRepository.findById(id).orElse(null)

    fun findByName(name: String, chatId: String?, user: User?): Group? {
        println("🟢 Поиск группы: name=$name, chatId=$chatId, user=${user?.username}")

        // 1. Ищем локальную группу (по chatId, доступную в этом чате)
        if (chatId != null) {
            val local = groupRepository.findByNameWithUsers(name, chatId)
            if (local != null && !local.isPrivate) return local
            if (local != null && local.isPrivate && (user == local.owner || user in local.allowedUsers)) return local
        }

        // 2. Приватные группы (chatId == null, isPrivate == true)
        if (user != null) {
            val byName = groupRepository.findAllByNameWithUsers(name)
            for (group in byName) {
                if (group.chatId == null && group.isPrivate &&
                    (group.owner?.id == user.id || group.allowedUsers.any { it.id == user.id })
                ) {
                    return group
                }
            }
        }

        // 3. Публичные глобальные группы (chatId == null, isPrivate == false)
        val global = groupRepository.findByNameWithUsers(name, null)
        if (global != null && !global.isPrivate) return global

        return null
    }


    fun findAllByName(name: String): List<Group> = groupRepository.findAllByName(name)

    fun findByChatId(chatId: String): Group? = groupRepository.findByChatId(chatId)

    fun findAll(): List<Group> = groupRepository.findAll()

    fun findAllWithUsers(): List<Group> = groupRepository.findAllWithUsers()

    fun findByOwnerGroupName(owner: User, name: String): Group? {
        return findAllByName(name).firstOrNull { it.owner?.id == owner.id }
    }

    fun findByNameWithUsers(name: String, chatId: String?): Group? =
        groupRepository.findByNameWithUsers(name, chatId)

    fun save(group: Group): Group = groupRepository.save(group)

    fun delete(id: UUID) = groupRepository.deleteById(id)

    fun delete(group: Group) = group.id?.let { delete(it) }

    fun createGroup(
        name: String,
        description: String = "",
        chatId: String? = null,
        isPrivate: Boolean = false,
        allowedUsers: Set<User> = emptySet(),
        notifiers: Set<User> = emptySet(),
        owner: User? = null
    ): Group {
        return groupRepository.save(
            Group(
                name = name,
                description = description,
                chatId = chatId,
                isPrivate = isPrivate,
                allowedUsers = allowedUsers.toMutableSet(),
                notifiers = notifiers.toMutableSet(),
                owner = owner
            )
        )
    }

    fun grantAccess(group: Group, user: User): Group {
        group.allowedUsers.add(user)

        // Превращаем локальную группу в глобальную приватную, если это первое делегирование
        if (!group.isPrivate && group.chatId != null && user != group.owner) {
            println("🔁 Преобразуем '${group.name}' в глобальную приватную")
            group.chatId = null
            group.isPrivate = true
        }

        return groupRepository.save(group)
    }


    fun grantNotifyRights(group: Group, user: User): Group {
        group.notifiers.add(user)
        return groupRepository.save(group)
    }

    fun isNotifier(group: Group, user: User): Boolean {
        return user == group.owner || group.notifiers.contains(user)
    }

    fun isAllowed(group: Group, user: User): Boolean {
        return user == group.owner || group.allowedUsers.contains(user)
    }
}