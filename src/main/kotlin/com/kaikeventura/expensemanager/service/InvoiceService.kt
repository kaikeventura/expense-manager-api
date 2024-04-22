package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.common.brazilZoneId
import com.kaikeventura.expensemanager.controller.request.StatementRequest
import com.kaikeventura.expensemanager.controller.response.InvoiceResponse
import com.kaikeventura.expensemanager.controller.response.InvoiceWithReferencesResponse
import com.kaikeventura.expensemanager.controller.response.StatementResponse
import com.kaikeventura.expensemanager.entity.InvoiceEntity
import com.kaikeventura.expensemanager.entity.InvoiceState.*
import com.kaikeventura.expensemanager.entity.UserEntity
import com.kaikeventura.expensemanager.error.exception.InvoiceNotFoundException
import com.kaikeventura.expensemanager.repository.InvoiceRepository
import com.kaikeventura.expensemanager.repository.StatementRepository
import com.kaikeventura.expensemanager.repository.UserRepository
import org.springframework.data.domain.Limit
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class InvoiceService(
    private val userRepository: UserRepository,
    private val invoiceRepository: InvoiceRepository,
    private val statementRepository: StatementRepository
) {

    fun createFirstInvoiceForAllUsers() {
        userRepository.findAll().forEach {
            invoiceRepository.save(
                InvoiceEntity(
                    referenceMonth = YearMonth.now(brazilZoneId()).toString(),
                    totalValue = 0L,
                    state = CURRENT,
                    user = it
                )
            )
        }
    }

    fun getInvoiceWithReferences(userEmail: String): List<InvoiceWithReferencesResponse> =
        invoiceRepository.findAllByUserEmail(userEmail).map {
            InvoiceWithReferencesResponse(
                referenceMonth = it.referenceMonth,
                state = it.state
            )
        }.sortedBy { YearMonth.parse(it.referenceMonth) }

    fun getInvoiceByReferenceMonth(userEmail: String, referenceMonth: YearMonth): InvoiceResponse =
        invoiceRepository.findByUserEmailAndReferenceMonth(userEmail, referenceMonth.toString())?.let { invoice ->
            InvoiceResponse(
                referenceMonth = invoice.referenceMonth,
                state = invoice.state,
                totalValue = invoice.totalValue,
                statements = statementRepository.findAllByInvoiceId(invoice.id!!).map { statement ->
                    StatementResponse(
                        code = statement.code,
                        description = statement.description,
                        category = statement.category,
                        value = statement.value,
                        type = statement.type,
                        createdAt = statement.createdAt!!.toString()
                    )
                }
            )
        } ?: throw InvoiceNotFoundException("Last invoice for user $userEmail and reference month $referenceMonth not found")

    fun createFutureInvoice(user: UserEntity): InvoiceEntity =
        invoiceRepository.findFirstByUserIdOrderByReferenceMonthDesc(user.id!!)?.let { lastInvoice ->
            invoiceRepository.save(
                InvoiceEntity(
                    referenceMonth = YearMonth.parse(lastInvoice.referenceMonth).plusMonths(1).toString(),
                    totalValue = 0L,
                    state = FUTURE,
                    user = user
                )
            )
        } ?: throw InvoiceNotFoundException("Last invoice for user ${user.id} not found")

    fun getInvoiceByUserIdAndReferenceMonth(userId: String, referenceMonth: YearMonth): InvoiceEntity =
        invoiceRepository.findByUserIdAndReferenceMonth(userId, referenceMonth.toString())
            ?: throw InvoiceNotFoundException("Invoice with reference month $referenceMonth for user $userId not found")

    fun updateInvoice(invoiceEntity: InvoiceEntity) = invoiceRepository.save(invoiceEntity)

    fun checkInvoicesAmount(user: UserEntity, statementRequest: StatementRequest) {
        invoiceRepository.countByUserIdAndReferenceMonthGreaterThanEqual(
            userId = user.id!!,
            referenceMonth = statementRequest.referenceMonth.toString()
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
            referenceMonth = referenceMonth.toString(),
            limit = Limit.of(limit)
        )

    fun getAllInvoices(user: UserEntity, referenceMonth: YearMonth): List<InvoiceEntity> =
        invoiceRepository.findAllByUserIdAndReferenceMonthGreaterThanEqual(
            userId = user.id!!,
            referenceMonth = referenceMonth.toString()
        )
}
