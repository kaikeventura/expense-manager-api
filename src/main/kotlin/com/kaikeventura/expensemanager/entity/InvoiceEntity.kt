package com.kaikeventura.expensemanager.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.time.YearMonth

@Entity
@Table(
    name = "invoices",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user", "referenceMonth"])
    ]
)
data class InvoiceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    val referenceMonth: YearMonth,

    val totalValue: Long,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    val state: InvoiceState,

    @CreationTimestamp
    @Column(updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    val modifiedAt: LocalDateTime? = null
)

enum class InvoiceState {
    PREVIOUS, CURRENT, FUTURE
}
