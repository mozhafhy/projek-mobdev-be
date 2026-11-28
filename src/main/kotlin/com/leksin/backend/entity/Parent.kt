package com.leksin.backend.entity

import com.leksin.backend.dto.response.ParentResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "parents")
class Parent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true, length = 50)
    var username: String,

    @Column(nullable = false, length = 255)
    var password: String?,

    @Column(nullable = false, unique = true, length = 255)
    var email: String,

    @Column(
        name = "email_verified",
        nullable = false,
        columnDefinition = "boolean default true",
    )
    var emailVerified: Boolean = true,
)

fun Parent.toParentResponse(): ParentResponse {
    return ParentResponse(
        id = this.id!!,
        username = this.username,
        email = this.email,
    )
}