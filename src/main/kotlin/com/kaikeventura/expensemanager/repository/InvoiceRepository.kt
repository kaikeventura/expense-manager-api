package com.kaikeventura.expensemanager.repository

import com.kaikeventura.expensemanager.entity.InvoiceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository : JpaRepository<InvoiceEntity, String> {
    fun findFirstByUserIdOrderByReferenceMonthDesc(userId: String): InvoiceEntity?
}
