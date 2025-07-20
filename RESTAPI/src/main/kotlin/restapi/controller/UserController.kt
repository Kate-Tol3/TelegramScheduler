package org.example.restapi.controller

import org.example.restapi.dto.CreateUserRequest
import org.example.restapi.dto.UserDto
import org.example.restapi.mapper.toDto
import org.example.storage.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    // GET /api/users
    @GetMapping
    fun getAllUsers(): List<UserDto> {
        return userService.findAll().map { it.toDto() }
    }

    // GET /api/users/{telegramId}
    @GetMapping("/{telegramId}")
    fun getByTelegramId(@PathVariable telegramId: Long): UserDto? {
        return userService.findByTelegramId(telegramId)?.toDto()
    }

    // POST /api/users
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): UserDto {
        val user = userService.create(
            telegramId = request.telegramId,
            chatId = request.chatId ?: request.telegramId.toString(), // если не передали chatId — используем telegramId
            username = request.username ?: "unknown"
        )
        return user.toDto()
    }
}
