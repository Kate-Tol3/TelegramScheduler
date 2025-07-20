package org.example.restapi.mapper

import org.example.restapi.dto.GroupDto
import org.example.storage.model.Group

fun Group.toDto(): GroupDto = GroupDto(
    id = this.id!!,
    name = this.name,
    description = this.description
)
