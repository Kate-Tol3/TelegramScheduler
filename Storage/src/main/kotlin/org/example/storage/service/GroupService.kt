package org.example.storage.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.example.storage.model.Group
import org.example.storage.model.User
import org.example.storage.repository.GroupRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupService(private val groupRepository: GroupRepository) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    fun findById(id: UUID): Group? = groupRepository.findById(id).orElse(null)

    fun findByName(
        name: String,
        chatId: String?,
        user: User?,
        isUserAdminInChat: (String) -> Boolean = { false }
    ): Group? {
        println("🟢 Поиск группы: name=$name, chatId=$chatId, user=${user?.username}")

        // 1. Локальная группа по chatId (если вызов из того же чата)
        if (chatId != null) {
            val local = groupRepository.findByNameWithUsers(name, chatId)
            if (local != null && !local.isPrivate) return local
            if (local != null && local.isPrivate && (user == local.owner || user in local.allowedUsers)) return local
        }

        // 1.5 — локальная публичная группа из другого чата, если пользователь админ в этом чате
        if (user != null) {
            val allNamed = groupRepository.findAllByNameWithUsers(name)
            val adminAccessibleLocal = allNamed.firstOrNull {
                it.chatId != null &&
                        it.owner == null &&
                        !it.isPrivate &&
                        isUserAdminInChat(it.chatId!!)
            }
            if (adminAccessibleLocal != null) return adminAccessibleLocal
        }

        // 2. Глобальная приватная
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

        // 3. Глобальная публичная
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

    fun deleteWithNotifications(group: Group, scheduledNotificationService: ScheduledNotificationService) {
        scheduledNotificationService.deleteAllByGroup(group)
        groupRepository.delete(group)
    }



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

        // Преобразуем в глобальную приватную (если это первое делегирование)
        if (!group.isPrivate && group.chatId != null && user != group.owner) {
            println("🔁 Преобразуем '${group.name}' в глобальную приватную")
            group.chatId = null
            group.isPrivate = true
            // 👉 Добавляем пометку владельца в название
            val suffix = " [от @${group.owner?.username ?: "owner"}]"
            group.name = if (!group.name.endsWith(suffix)) group.name + suffix else group.name
        }

        return groupRepository.save(group)
    }


    @Transactional
    fun grantNotifyRights(group: Group, user: User): Group {
        // Убедимся, что notifiers содержит именно Managed-сущности
        val already = group.notifiers.any { it.id == user.id }
        if (!already) {
            // Найдём user через entityManager
            val managedUser = group.allowedUsers.firstOrNull { it.id == user.id }
                ?: group.owner?.takeIf { it.id == user.id }
                ?: throw IllegalStateException("User with id=${user.id} is not allowed in this group")

            group.notifiers.add(managedUser)
        }

        return groupRepository.save(group)
    }



    fun isNotifier(group: Group, user: User): Boolean {
        return group.owner?.id == user.id || group.notifiers.any { it.id == user.id }
    }


    fun isAllowed(group: Group, user: User): Boolean {
        return user == group.owner || group.allowedUsers.contains(user)
    }

    fun findByNameInternal(name: String, chatId: String?): Group? {
        val normalizedChatId = when (name.lowercase()) {
            "backend", "frontend", "devops", "design", "all" -> null
            else -> chatId?.ifBlank { null }
        }

        println("🔍 Внутренний поиск группы (без проверок доступа): name=$name, chatId=$normalizedChatId")

        val localOrGlobal = groupRepository.findByNameWithUsers(name, normalizedChatId)
        if (localOrGlobal != null) return localOrGlobal

        val candidates = groupRepository.findAllByNameWithUsers(name)
        return candidates.firstOrNull { it.chatId == null && it.isPrivate }
    }






}