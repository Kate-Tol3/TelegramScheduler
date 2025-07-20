package org.example.restapi.controller

import org.example.restapi.dto.SubscribeRequest
import org.example.restapi.dto.SubscriptionDto
import org.example.restapi.mapper.toDto
import org.example.storage.service.GroupService
import org.example.storage.service.SubscriptionService
import org.example.storage.service.UserService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val userService: UserService,
    private val groupService: GroupService,
    private val subscriptionService: SubscriptionService
) {

    // GET /api/subscriptions/{userId}
    @GetMapping("/{userId}")
    fun getUserSubscriptions(@PathVariable userId: UUID): List<SubscriptionDto> {
        val user = userService.findById(userId)
            ?: throw IllegalArgumentException("User not found: $userId")
        return subscriptionService.findByUser(user).map { it.toDto() }
    }

    // POST /api/subscriptions
    @PostMapping
    fun subscribe(@RequestBody request: SubscribeRequest): SubscriptionDto {
        val user = userService.findById(request.userId)
            ?: throw IllegalArgumentException("User not found: ${request.userId}")
        val group = groupService.findById(request.groupId)
            ?: throw IllegalArgumentException("Group not found: ${request.groupId}")

        val success = subscriptionService.subscribe(user, group)
        if (!success) {
            throw IllegalStateException("User is already subscribed")
        }

        return subscriptionService.findByUserAndGroup(user, group)!!.toDto()
    }

    // DELETE /api/subscriptions
    @DeleteMapping
    fun unsubscribe(@RequestBody request: SubscribeRequest) {
        val user = userService.findById(request.userId) ?: return
        val group = groupService.findById(request.groupId) ?: return
        subscriptionService.unsubscribe(user, group)
    }
}
