package org.example.bot.repository

import org.example.bot.model.Word
import org.springframework.data.jpa.repository.JpaRepository

interface WordRepository : JpaRepository<Word, Long>
