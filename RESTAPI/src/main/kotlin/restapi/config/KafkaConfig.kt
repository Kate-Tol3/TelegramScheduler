package org.example.restapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.example.messaging.dto.NotificationMessage
import org.example.restapi.dto.ScheduleNotificationRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
open class KafkaConfig {

    private fun objectMapper(): ObjectMapper =
        ObjectMapper().registerKotlinModule()

    @Bean
    open fun notificationMessageProducerFactory(): ProducerFactory<String, NotificationMessage> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        return DefaultKafkaProducerFactory(
            configProps,
            StringSerializer(),
            JsonSerializer(objectMapper())
        )
    }

    @Bean
    open fun scheduleRequestProducerFactory(): ProducerFactory<String, ScheduleNotificationRequest> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        return DefaultKafkaProducerFactory(
            configProps,
            StringSerializer(),
            JsonSerializer(objectMapper())
        )
    }

    @Bean
    open fun notificationMessageKafkaTemplate(): KafkaTemplate<String, NotificationMessage> =
        KafkaTemplate(notificationMessageProducerFactory())

    @Bean
    open fun scheduleRequestKafkaTemplate(): KafkaTemplate<String, ScheduleNotificationRequest> =
        KafkaTemplate(scheduleRequestProducerFactory())
}
