package org.example.storage.repository

import org.example.storage.model.Group
import org.example.storage.model.ScheduledNotification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

interface ScheduledNotificationRepository : JpaRepository<ScheduledNotification, UUID> {

    @Query("SELECT s FROM ScheduledNotification s JOIN FETCH s.targetUsers")
    fun findAllWithUsers(): List<ScheduledNotification>

    @Query("""
    SELECT s
    FROM ScheduledNotification s
    LEFT JOIN FETCH s.targetGroups
    WHERE s.eventTime <= CURRENT_TIMESTAMP AND s.dispatched = false
""")
    fun findDueWithGroups(): List<ScheduledNotification>

    @Query("""
    SELECT DISTINCT s
    FROM ScheduledNotification s
    LEFT JOIN FETCH s.targetGroups
    LEFT JOIN FETCH s.targetUsers
    WHERE s.eventTime <= CURRENT_TIMESTAMP AND s.dispatched = false
""")
    fun findDueWithGroupsAndUsers(): List<ScheduledNotification>

    @Query(
        """
    SELECT DISTINCT s 
    FROM ScheduledNotification s 
    LEFT JOIN FETCH s.targetGroups 
    LEFT JOIN FETCH s.targetUsers 
    WHERE s.eventTime <= :time AND s.dispatched = false
    """
    )
    fun findDueWithTargets(@Param("time") time: LocalDateTime): List<ScheduledNotification>

    @Query(
        """
    SELECT s FROM ScheduledNotification s
    JOIN s.targetGroups g
    WHERE g = :group
    """
    )
    fun findAllByGroup(@Param("group") group: Group): List<ScheduledNotification>



    fun findAllByEventTimeBeforeAndDispatchedFalse(time: LocalDateTime): List<ScheduledNotification>

}
