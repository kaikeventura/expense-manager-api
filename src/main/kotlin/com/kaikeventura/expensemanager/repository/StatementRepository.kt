package com.kaikeventura.expensemanager.repository

import com.kaikeventura.expensemanager.entity.StatementEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StatementRepository : JpaRepository<StatementEntity, String> {
    fun findAllByInvoiceId(invoiceId: String): List<StatementEntity>
    fun findAllByInvoiceUserId(userId: String): List<StatementEntity>
}
