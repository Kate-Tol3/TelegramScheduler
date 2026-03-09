package org.example.storage.service

import jakarta.transaction.Transactional
import org.example.storage.model.Group
import org.example.storage.model.Subscription
import org.example.storage.model.User
import org.example.storage.repository.GroupRepository
import org.example.storage.repository.SubscriptionRepository
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val groupRepository: GroupRepository
) {

    fun findAll(): List<Subscription> = subscriptionRepository.findAll()

    fun subscribe(user: User, group: Group): Boolean {
        val loadedGroup = groupRepository.findByNameWithUsers(group.name, group.chatId)
            ?: return false // если группа не найдена (должно быть редкостью)

        println("📋 Проверка доступа к группе '${loadedGroup.name}'")
        println("🔒 isPrivate: ${loadedGroup.isPrivate}")
        println("👤 Пользователь: ${user.username} (id=${user.id})")
        println("🧑‍🤝‍🧑 allowedUsers:")
        loadedGroup.allowedUsers.forEach {
            println("   - ${it.username} (id=${it.id})")
        }
        println("👑 Владелец: ${loadedGroup.owner?.username} (id=${loadedGroup.owner?.id})")

        //Проверка доступа
        if (
            loadedGroup.isPrivate &&
            loadedGroup.allowedUsers.none { it.id == user.id } &&
            loadedGroup.owner?.id != user.id
        ) {
            println("❌ Нет доступа: пользователь не найден среди allowedUsers и не является владельцем.")
            return false
        }


        val existing = subscriptionRepository.findByUserAndGroup(user, loadedGroup)
        if (existing != null) return false

        val subscription = Subscription(
            user = user,
            group = loadedGroup,
            groupName = loadedGroup.name
        )

        subscriptionRepository.save(subscription)
        return true
    }


    fun unsubscribe(user: User, group: Group): Boolean {
        val existing = subscriptionRepository.findByUserAndGroup(user, group)
        return if (existing != null) {
            subscriptionRepository.delete(existing)
            true
        } else false
    }

    fun isSubscribed(user: User, group: Group): Boolean {
        return subscriptionRepository.findByUserAndGroup(user, group) != null
    }

    fun findByUser(user: User): List<Subscription> = subscriptionRepository.findByUser(user)

    fun findUsersByGroup(group: Group): List<User> {
        return subscriptionRepository.findByGroup(group).map { it.user }.distinct()
    }

    @Transactional
    fun deleteAllByGroup(group: Group) {
        val subs = subscriptionRepository.findAllByGroup(group)
        subscriptionRepository.deleteAll(subs)
    }


    fun findUsersByGroupNameAndChatId(
        groupName: String,
        chatId: String?,
        groupService: GroupService,
        user: User
    ): List<User> {
        val group = groupService.findByName(groupName, chatId, user) ?: return emptyList()
        return findUsersByGroup(group)
    }

    fun findByUserAndGroup(user: User, group: Group): Subscription? =
        subscriptionRepository.findByUserAndGroup(user, group)
}
