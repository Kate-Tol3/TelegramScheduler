//package org.example.scheduler.controller
//
//import org.example.messaging.dto.NotificationMessage
//import org.example.scheduler.kafka.NotificationKafkaProducer
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//
//@RestController
//@RequestMapping("/test")
//class TestSchedulerController(
//    private val producer: NotificationKafkaProducer
//) {
//
//    @PostMapping("/send")
//    fun send(
//        @RequestParam group: String,
//        @RequestParam text: String
//    ): ResponseEntity<String> {
//        val message = NotificationMessage(
//            type = "MANUAL",         // или "CALL", "MR"
//            text = text,
//            groupName = group
//        )
//        producer.sendNotification(message)
//        return ResponseEntity.ok("Сообщение отправлено в Kafka")
//    }
//}
