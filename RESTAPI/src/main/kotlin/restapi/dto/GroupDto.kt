package org.example.restapi.dto

import java.util.*

data class GroupDto(
    val id: UUID,
    val name: String,
    val description: String
)

data class CreateGroupRequest(
    val name: String,
    val description: String
)
