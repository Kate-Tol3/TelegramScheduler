//package org.example.storage.model
//
//import jakarta.persistence.*
//import java.time.LocalDateTime
//import java.util.UUID
//
//@Entity
//@Table(name = "delivery_logs")
//class DeliveryLog(
//    @Id
//    @GeneratedValue
//    val id: UUID? = null,
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    val user: User,
//
//    @ManyToOne
//    @JoinColumn(name = "notification_id", nullable = false)
//    val notification: Notification,
//
//    val sentAt: LocalDateTime = LocalDateTime.now()
//)
