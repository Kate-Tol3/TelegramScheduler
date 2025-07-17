package org.example.storage.repository

import org.example.storage.model.Group
import org.example.storage.model.Subscription
import org.example.storage.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface
SubscriptionRepository : JpaRepository<Subscription, UUID> {
    fun findByUserId(userId: UUID): List<Subscription>
    fun findByGroupId(groupId: UUID): List<Subscription>
    fun findByUserAndGroup(user: User, group: Group): Subscription?
    fun findByUser(user: User): List<Subscription>
    fun findByGroup(group: Group): List<Subscription>
}