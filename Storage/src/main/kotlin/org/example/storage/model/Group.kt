package org.example.storage.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "groups")
class Group(
    @Id @GeneratedValue val id: UUID? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = true)
    var chatId: String? = null,

    @Column(nullable = false)
    var isPrivate: Boolean = false,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    var owner: User? = null,

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "group_allowed_users",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var allowedUsers: MutableSet<User> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "group_notifiers",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var notifiers: MutableSet<User> = mutableSetOf()

)
