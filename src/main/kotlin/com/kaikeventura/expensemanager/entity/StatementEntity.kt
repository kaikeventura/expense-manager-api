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

    val description: Long,

    val value: Long,

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    val invoice: InvoiceEntity,

    @CreationTimestamp
    @Column(updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    val modifiedAt: LocalDateTime? = null
)
