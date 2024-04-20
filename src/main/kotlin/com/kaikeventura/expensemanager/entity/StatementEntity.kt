package com.kaikeventura.expensemanager.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "statements")
data class StatementEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    val description: String,

    val value: Long,

    val type: StatementType,

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    val invoice: InvoiceEntity,

    @CreationTimestamp
    @Column(updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    val modifiedAt: LocalDateTime? = null
)

enum class StatementType(description: String) {
    CREDIT_CARD("Cartão de crédito"),
    FIXED("Gasto fixo"),
    IN_CASH("À vista")
}
