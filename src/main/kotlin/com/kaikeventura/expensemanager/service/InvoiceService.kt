package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.common.brazilZoneId
import com.kaikeventura.expensemanager.controller.request.StatementRequest
import com.kaikeventura.expensemanager.entity.InvoiceEntity
import com.kaikeventura.expensemanager.entity.InvoiceState.*
import com.kaikeventura.expensemanager.entity.UserEntity
import com.kaikeventura.expensemanager.error.exception.InvoiceNotFoundException
import com.kaikeventura.expensemanager.repository.InvoiceRepository
import com.kaikeventura.expensemanager.repository.UserRepository
import org.springframework.data.domain.Limit
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class InvoiceService(
    private val userRepository: UserRepository,
    private val invoiceRepository: InvoiceRepository
) {

    fun createFirstInvoiceForAllUsers() {
        userRepository.findAll().forEach {
            invoiceRepository.save(
                InvoiceEntity(
                    referenceMonth = YearMonth.now(brazilZoneId()),
                    totalValue = 0L,
                    state = CURRENT,
                    user = it
                )
            )
        }
    }

    fun createFutureInvoice(user: UserEntity): InvoiceEntity =
        invoiceRepository.findFirstByUserIdOrderByReferenceMonthDesc(user.id!!)?.let { lastInvoice ->
            invoiceRepository.save(
                InvoiceEntity(
                    referenceMonth = lastInvoice.referenceMonth.plusMonths(1),
                    totalValue = 0L,
                    state = FUTURE,
                    user = user
                )
            )
        } ?: throw InvoiceNotFoundException("Last invoice for user ${user.id} not found")

    fun getInvoiceByUserIdAndReferenceMonth(userId: String, referenceMonth: YearMonth): InvoiceEntity =
        invoiceRepository.findByUserIdAndReferenceMonth(userId, referenceMonth)
            ?: throw InvoiceNotFoundException("Invoice with reference month $referenceMonth for user $userId not found")

    fun updateInvoice(invoiceEntity: InvoiceEntity) = invoiceRepository.save(invoiceEntity)

    fun checkInvoicesAmount(user: UserEntity, statementRequest: StatementRequest) {
        invoiceRepository.countByUserIdAndReferenceMonthGreaterThanEqual(
            userId = user.id!!,
            referenceMonth = statementRequest.referenceMonth
        ).let { invoicesAmount ->
            val diff = statementRequest.installmentAmount!! - invoicesAmount
            if (diff > 0) {
                (1..diff).forEach { _ ->
                    createFutureInvoice(user)
                }
            }
        }
    }

    fun getInvoicesLimitedTo(user: UserEntity, referenceMonth: YearMonth, limit: Int): List<InvoiceEntity> =
        invoiceRepository.findAllByUserIdAndReferenceMonthGreaterThanEqual(
            userId = user.id!!,
            referenceMonth = referenceMonth,
            limit = Limit.of(limit)
        )

    fun getAllInvoices(user: UserEntity, referenceMonth: YearMonth): List<InvoiceEntity> =
        invoiceRepository.findAllByUserIdAndReferenceMonthGreaterThanEqual(
            userId = user.id!!,
            referenceMonth = referenceMonth
        )
}
