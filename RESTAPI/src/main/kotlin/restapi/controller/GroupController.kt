package org.example.restapi.controller

import org.example.restapi.dto.CreateGroupRequest
import org.example.restapi.dto.GroupDto
import org.example.restapi.mapper.toDto
import org.example.storage.service.GroupService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
class GroupController(
    private val groupService: GroupService
) {
    @GetMapping
    fun getAllGroups(): List<GroupDto> {
        return groupService.findAll().map { it.toDto() }
    }

    @PostMapping
    fun createGroup(@RequestBody request: CreateGroupRequest): GroupDto {
        val group = groupService.createGroup(request.name, request.description)
        return group.toDto()
    }
}
