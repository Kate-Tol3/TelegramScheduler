package org.example.storage.repository

import org.example.storage.model.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SubscriptionRepository : JpaRepository<Subscription, UUID> {
    fun findByUserId(userId: UUID): List<Subscription>
    fun findByGroupId(groupId: UUID): List<Subscription>
}