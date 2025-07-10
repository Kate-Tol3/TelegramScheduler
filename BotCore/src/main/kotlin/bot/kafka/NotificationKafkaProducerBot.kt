//package org.example.bot.kafka
//
//import org.springframework.kafka.core.KafkaTemplate
//import org.springframework.stereotype.Service
//
//@Service
//class NotificationKafkaProducerBot(
//    private val kafkaTemplate: KafkaTemplate<String, String>
//) {
//    fun send(topic: String, message: String) {
//        println("🟢 Отправляю в Kafka: $message")
//        kafkaTemplate.send(topic, message)
//    }
//}
//a