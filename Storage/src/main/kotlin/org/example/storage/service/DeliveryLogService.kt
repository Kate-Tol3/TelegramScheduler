//package org.example.storage.service
//
//import org.example.storage.model.DeliveryLog
//import org.example.storage.repository.*
//import org.springframework.stereotype.Service
//import java.util.*
//
//@Service
//class DeliveryLogService(private val deliveryLogRepository: DeliveryLogRepository) {
//    fun findById(id: UUID): DeliveryLog? = deliveryLogRepository.findById(id).orElse(null)
//    fun save(deliveryLog: DeliveryLog): DeliveryLog = deliveryLogRepository.save(deliveryLog)
//    fun findAll(): List<DeliveryLog> = deliveryLogRepository.findAll()
//    fun delete(id: UUID) = deliveryLogRepository.deleteById(id)
//}