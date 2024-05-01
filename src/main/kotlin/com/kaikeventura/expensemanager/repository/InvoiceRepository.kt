package com.kaikeventura.expensemanager.repository

import com.kaikeventura.expensemanager.entity.InvoiceEntity
import com.kaikeventura.expensemanager.entity.InvoiceState
import com.kaikeventura.expensemanager.entity.InvoiceWithReferenceProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository : JpaRepository<InvoiceEntity, String> {
    fun findFirstByUserIdOrderByReferenceMonthDesc(userId: String): InvoiceEntity?
    fun findByUserIdAndReferenceMonth(userId: String, referenceMonth: String): InvoiceEntity?
    fun countByUserIdAndReferenceMonthGreaterThanEqual(userId: String, referenceMonth: String): Int
    fun findAllByUserIdAndReferenceMonthGreaterThanEqual(userId: String, referenceMonth: String): List<InvoiceEntity>
    fun findAllByUserEmail(userEmail: String): List<InvoiceWithReferenceProjection>
    fun findByUserEmailAndReferenceMonth(userEmail: String, referenceMonth: String): InvoiceEntity?
    fun findAllByState(state: InvoiceState): List<InvoiceEntity>
    fun findAllByUserId(userId: String): List<InvoiceEntity>
    fun findByUserIdAndReferenceMonthAndState(userId: String, referenceMonth: String, state: InvoiceState): InvoiceEntity?
    fun findByUserIdAndState(userId: String, state: InvoiceState): InvoiceEntity?

    @Query(
        value = """
            SELECT i.* FROM invoices i
            LEFT JOIN users u ON i.user_id = u.id
            WHERE u.id = :userId
            AND i.reference_month >= :referenceMonth
            ORDER BY i.reference_month
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findAllByUserIdAndReferenceMonthGreaterThanEqualLimitedTo(userId: String, referenceMonth: String, limit: Int): List<InvoiceEntity>
}
