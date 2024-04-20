package com.kaikeventura.expensemanager.repository

import com.kaikeventura.expensemanager.entity.InvoiceEntity
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.YearMonth

@Repository
interface InvoiceRepository : JpaRepository<InvoiceEntity, String> {
    fun findFirstByUserIdOrderByReferenceMonthDesc(userId: String): InvoiceEntity?
    fun findByUserIdAndReferenceMonth(userId: String, referenceMonth: YearMonth): InvoiceEntity?
    fun countByUserIdAndReferenceMonthGreaterThanEqual(userId: String, referenceMonth: YearMonth): Int
    fun findAllByUserIdAndReferenceMonthGreaterThanEqual(userId: String, referenceMonth: YearMonth, limit: Limit): List<InvoiceEntity>
}
