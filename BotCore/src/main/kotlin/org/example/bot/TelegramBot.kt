package org.example.bot

import org.example.bot.model.Word
import org.example.bot.repository.WordRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBot(
    private val botProperties: BotProperties,
    private val wordRepository: WordRepository
) : TelegramLongPollingBot() {

    override fun getBotUsername(): String = botProperties.username
    override fun getBotToken(): String = botProperties.token

    override fun onUpdateReceived(update: Update) {
        val message = update.message ?: return
        val text = message.text ?: return
        val chatId = message.chatId.toString()

        wordRepository.save(Word(text = text))

        execute(SendMessage(chatId, "Слово '$text' сохранено в базе данных!"))
    }
}
