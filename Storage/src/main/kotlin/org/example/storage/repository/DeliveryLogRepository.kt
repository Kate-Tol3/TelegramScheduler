//package org.example.storage.repository
//
//import org.example.storage.model.DeliveryLog
//import org.springframework.data.jpa.repository.JpaRepository
//import java.util.*
//
//interface DeliveryLogRepository : JpaRepository<DeliveryLog, UUID> {
//    fun findByNotificationId(notificationId: UUID): List<DeliveryLog>
//}