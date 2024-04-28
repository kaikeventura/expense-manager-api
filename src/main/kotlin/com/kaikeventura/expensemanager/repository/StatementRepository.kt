package com.kaikeventura.expensemanager.repository

import com.kaikeventura.expensemanager.entity.StatementEntity
import com.kaikeventura.expensemanager.entity.StatementType
import org.springframework.data.jpa.repository.JpaRepository

interface StatementRepository : JpaRepository<StatementEntity, String> {
    fun findAllByInvoiceId(invoiceId: String): List<StatementEntity>
    fun findAllByInvoiceIdAndType(invoiceId: String, type: StatementType): List<StatementEntity>
    fun findAllByInvoiceUserId(userId: String): List<StatementEntity>
}
