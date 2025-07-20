// SubscriptionDto.kt
package org.example.restapi.dto

import java.util.*

data class SubscriptionDto(
    val userId: UUID,
    val groupId: UUID,
    val groupName: String
)

data class SubscribeRequest(
    val userId: UUID,
    val groupId: UUID
)
