package org.example.storage.service

import org.example.storage.model.Group
import org.example.storage.model.Subscription
import org.example.storage.model.User
import org.example.storage.repository.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class SubscriptionService(private val subscriptionRepository: SubscriptionRepository) {
    //fun findById(id: UUID): Subscription? = subscriptionRepository.findById(id).orElse(null)
    //fun save(subscription: Subscription): Subscription = subscriptionRepository.save(subscription)
    fun findAll(): List<Subscription> = subscriptionRepository.findAll()
    //fun delete(id: UUID) = subscriptionRepository.deleteById(id)


    fun subscribe(user: User, group: Group): Boolean {
        val existing = subscriptionRepository.findByUserAndGroup(user, group)
        if (existing != null) return false

        val subscription = Subscription(
            user = user,
            group = group,
            groupName = group.name // ← вот здесь явно сохраняем
        )

        subscriptionRepository.save(subscription)
        return true
    }

    fun unsubscribe(user: User, group: Group): Boolean {
        val existing = subscriptionRepository.findByUserAndGroup(user, group)
        return if (existing != null) {
            subscriptionRepository.delete(existing)
            true
        } else {
            false
        }
    }

    fun findByUser(user: User): List<Subscription> = subscriptionRepository.findByUser(user)
    fun findUsersByGroup(group: Group): List<User> {
        return subscriptionRepository.findByGroup(group).map { it.user }.distinct()
    }


}