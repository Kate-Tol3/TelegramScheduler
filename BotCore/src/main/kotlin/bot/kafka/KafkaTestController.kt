//package org.example.bot.kafka
//
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestParam
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//class KafkaTestController(
//    private val kafkaProducer: NotificationKafkaProducerBot
//) {
//
//    @GetMapping("/kafka/test")
//    fun sendTestMessage(
//        @RequestParam message: String = "🧪 Тестовое сообщение из REST"
//    ): String {
//        kafkaProducer.send("notification-send", message)
//        return "Сообщение отправлено: $message"
//    }
//}
