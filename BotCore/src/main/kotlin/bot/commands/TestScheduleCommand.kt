//package org.example.bot.commands
//
//import org.example.storage.model.*
//import org.example.storage.repository.*
//import org.springframework.stereotype.Component
//import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage
//import org.telegram.telegrambots.meta.api.objects.Chat
//import org.telegram.telegrambots.meta.api.objects.User as TelegramUser
//import org.telegram.telegrambots.meta.bots.AbsSender
//import java.time.LocalDateTime
//
//@Component
//class TestScheduleCommand(
//    private val userRepository: UserRepository,
//    private val templateRepository: TemplateRepository,
//    private val eventRepository: EventRepository,
//    private val scheduledNotificationRepository: ScheduledNotificationRepository
//) : BotCommand("test_schedule", "Создать тестовое запланированное уведомление") {
//
//    override fun execute(sender: AbsSender, user: TelegramUser, chat: Chat, arguments: Array<String>) {
//        // 1. Найти или создать пользователя
//        val dbUser = userRepository.findByTelegramId(user.id)
//            ?: userRepository.save(
//                User(
//                    telegramId = user.id,
//                    chatId = chat.id.toString(),
//                    username = user.userName ?: "unknown"
//                )
//            )
//
//        // 2. Создаём шаблон
//        val template = templateRepository.save(
//            Template(
//                eventType = EventType.CALL,
//                text = "Привет, {{username}}! Это тестовое уведомление из планировщика.",
//                channel = Channel.PRIVATE
//            )
//        )
//
//        // 3. Создаём событие
//        val event = eventRepository.save(
//            Event(
//                type = EventType.CALL, // 👈 ОБЯЗАТЕЛЬНО
//                payload = mapOf("username" to dbUser.username)
//            )
//        )
//
//        // 4. Создаём ScheduledNotification сразу с нужным пользователем
//        val scheduled = ScheduledNotification(
//            template = template,
//            event = event,
//            eventTime = LocalDateTime.now().plusSeconds(10),
//            repeatCount = 1,
//            targetUsers = setOf(dbUser) // ✅ передаём Set напрямую
//        )
//
//        scheduledNotificationRepository.save(scheduled)
//
//        // 5. Ответ пользователю
//        sender.execute(
//            SendMessage(chat.id.toString(), "✅ Уведомление запланировано через 10 секунд.")
//        )
//    }
//}
