package com.kaikeventura.expensemanager.repository

import com.kaikeventura.expensemanager.entity.InvoiceEntity
import com.kaikeventura.expensemanager.entity.InvoiceWithReferenceProjection
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository : JpaRepository<InvoiceEntity, String> {
    fun findFirstByUserIdOrderByReferenceMonthDesc(userId: String): InvoiceEntity?
    fun findByUserIdAndReferenceMonth(userId: String, referenceMonth: String): InvoiceEntity?
    fun countByUserIdAndReferenceMonthGreaterThanEqual(userId: String, referenceMonth: String): Int
    fun findAllByUserIdAndReferenceMonthGreaterThanEqual(userId: String, referenceMonth: String, limit: Limit): List<InvoiceEntity>
    fun findAllByUserIdAndReferenceMonthGreaterThanEqual(userId: String, referenceMonth: String): List<InvoiceEntity>
    fun findAllByUserEmail(userEmail: String): List<InvoiceWithReferenceProjection>
    fun findByUserEmailAndReferenceMonth(userEmail: String, referenceMonth: String): InvoiceEntity?
}
