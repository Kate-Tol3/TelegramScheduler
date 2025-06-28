package org.example.storage.model

import jakarta.persistence.*
import java.util.UUID

//	Связь между User и Group (многие-ко-многим)
@Entity
@Table(name = "subscriptions")
class Subscription(
    @Id @GeneratedValue val id: UUID? = null,

    @ManyToOne @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne @JoinColumn(name = "group_id")
    val group: Group,

    @Column(nullable = false)
    val groupName: String = group.name // автоматически копируем имя
)



