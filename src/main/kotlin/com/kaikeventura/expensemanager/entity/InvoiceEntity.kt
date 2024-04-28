package com.kaikeventura.expensemanager.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "invoices")
data class InvoiceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(length = 7)
    val referenceMonth: String,

    val totalValue: Long,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @Enumerated(EnumType.STRING)
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
