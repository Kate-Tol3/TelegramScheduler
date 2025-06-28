package org.example.storage.service

import org.example.storage.model.Subscription
import org.example.storage.repository.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class SubscriptionService(private val subscriptionRepository: SubscriptionRepository) {
    fun findById(id: UUID): Subscription? = subscriptionRepository.findById(id).orElse(null)
    fun save(subscription: Subscription): Subscription = subscriptionRepository.save(subscription)
    fun findAll(): List<Subscription> = subscriptionRepository.findAll()
    fun delete(id: UUID) = subscriptionRepository.deleteById(id)
}