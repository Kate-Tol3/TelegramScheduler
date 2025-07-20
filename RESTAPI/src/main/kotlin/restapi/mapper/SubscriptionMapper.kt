// SubscriptionMapper.kt
package org.example.restapi.mapper

import org.example.restapi.dto.SubscriptionDto
import org.example.storage.model.Subscription

fun Subscription.toDto(): SubscriptionDto = SubscriptionDto(
    userId = this.user.id!!,
    groupId = this.group.id!!,
    groupName = this.groupName
)
