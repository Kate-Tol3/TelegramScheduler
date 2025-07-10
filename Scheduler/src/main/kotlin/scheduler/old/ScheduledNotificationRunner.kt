//package org.example.scheduler.old
//
//import org.example.bot.sender.NotificationSender
//import org.example.storage.model.Channel
//import org.example.storage.service.ScheduledNotificationService
//import org.slf4j.LoggerFactory
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Component
//import org.telegram.telegrambots.meta.bots.AbsSender
//import java.time.LocalDateTime
//
//@Component
//class ScheduledNotificationRunner(
//    private val scheduledNotificationService: ScheduledNotificationService,
//    private val notificationSender: NotificationSender,
//    private val telegramBot: AbsSender //пока так
//) {
//    private val logger = LoggerFactory.getLogger(ScheduledNotificationRunner::class.java)
//
//    @Scheduled(fixedDelay = 60000)
//    fun run() {
//        val now = LocalDateTime.now()
//
//        val scheduledList = scheduledNotificationService.findAllWithUsers()
//            .filter { it.eventTime <= now && it.repeatCount > 0 }
//
//        for (scheduled in scheduledList) {
//            logger.info("Обработка уведомления: ${scheduled.id}")
//            val template = scheduled.template
//            val event = scheduled.event
//
//            val message = notificationSender.applyTemplate(template, event.payload)
//
//            if (template.channel == Channel.PRIVATE || template.channel == Channel.BOTH) {
//                notificationSender.sendToUsers(telegramBot, scheduled.targetUsers, message)
//            }
//
//            // (опционально: рассылка в группы)
//
//            scheduledNotificationService.decreaseRepeatOrRemove(scheduled)
//        }
//    }
//}
