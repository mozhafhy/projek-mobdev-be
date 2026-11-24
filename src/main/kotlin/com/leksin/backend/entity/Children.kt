package com.leksin.backend.entity

import com.leksin.backend.dto.ChildResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "children",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_child_parent_nickname",
            columnNames = ["parent_id", "nickname"],
        )
    ]
)
class Children(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, length = 32)
    var nickname: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    var parent: Parent,
)

fun Children.toChildResponse(): ChildResponse {
    return ChildResponse(
        id = this.id!!,
        nickname = this.nickname,
    )
}
