package org.example.bot.model

import jakarta.persistence.*

@Entity
data class Word(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val text: String
)
