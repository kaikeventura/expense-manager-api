package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.common.brazilZoneId
import com.kaikeventura.expensemanager.common.toYearMonth
import com.kaikeventura.expensemanager.controller.request.StatementRequest
import com.kaikeventura.expensemanager.controller.response.InvoiceResponse
import com.kaikeventura.expensemanager.controller.response.InvoiceWithReferencesResponse
import com.kaikeventura.expensemanager.controller.response.StatementResponse
import com.kaikeventura.expensemanager.entity.InvoiceEntity
import com.kaikeventura.expensemanager.entity.InvoiceState.*
import com.kaikeventura.expensemanager.entity.StatementEntity
import com.kaikeventura.expensemanager.entity.StatementType.FIXED
import com.kaikeventura.expensemanager.entity.UserEntity
import com.kaikeventura.expensemanager.error.exception.InvoiceNotFoundException
import com.kaikeventura.expensemanager.repository.InvoiceRepository
import com.kaikeventura.expensemanager.repository.StatementRepository
import com.kaikeventura.expensemanager.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.util.UUID

@Service
class InvoiceService(
    private val userRepository: UserRepository,
    private val invoiceRepository: InvoiceRepository,
    private val statementRepository: StatementRepository
) {

    fun createFirstInvoiceForAllUsers() {
        userRepository.findAllWithoutInvoice().forEach {
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

    fun recalculateTotalValue(invoiceId: UUID) {
        invoiceRepository.findByIdOrNull(invoiceId.toString())?.let { invoice ->
            updateInvoice(
                invoice = invoice.copy(
                    totalValue = invoice.sumStatementValues()
                )
            )
        } ?: throw InvoiceNotFoundException("Invoice $invoiceId not found")
    }

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

    fun updateInvoice(invoice: InvoiceEntity) = invoiceRepository.save(invoice)

    fun checkInvoicesAmount(user: UserEntity, statementRequest: StatementRequest) {
        invoiceRepository.countByUserIdAndReferenceMonthGreaterThanEqual(
            userId = user.id!!,
            referenceMonth = statementRequest.referenceMonth.toString()
        ).let { invoicesAmount ->
            val diff = statementRequest.installmentAmount!! - invoicesAmount
            if (diff > 0) {
                handleFutureInvoices(user, diff)
            }
        }
    }

    fun getInvoicesLimitedTo(user: UserEntity, referenceMonth: YearMonth, limit: Int): List<InvoiceEntity> =
        invoiceRepository.findAllByUserIdAndReferenceMonthGreaterThanEqualLimitedTo(
            userId = user.id!!,
            referenceMonth = referenceMonth.toString(),
            limit = limit
        )

    fun getAllInvoices(user: UserEntity, referenceMonth: YearMonth): List<InvoiceEntity> =
        invoiceRepository.findAllByUserIdAndReferenceMonthGreaterThanEqual(
            userId = user.id!!,
            referenceMonth = referenceMonth.toString()
        )

    fun handleInvoiceCycles() {
        val currentYearMonth = YearMonth.now(brazilZoneId())

        invoiceRepository.findAllByState(CURRENT).forEach { invoice ->
            with(invoice) {
                if (referenceMonth.toYearMonth() < currentYearMonth) {
                    handleInvoiceCycle(currentYearMonth)
                }
            }
        }
    }

    fun getCurrentInvoiceIdByUserEmail(userEmail: String): UUID =
        invoiceRepository.findCurrentInvoiceByUserEmail(userEmail)
            ?: throw InvoiceNotFoundException("Current invoice for user $userEmail not found")

    private fun InvoiceEntity.handleInvoiceCycle(currentYearMonth: YearMonth) {
        updateInvoice(
            invoice = this.copy(state = PREVIOUS)
        )
        getNextInvoice()?.let { nextInvoice ->
            updateInvoice(
                invoice = nextInvoice.copy(state = CURRENT)
            )
        } ?: createNewCurrentInvoice(
            currentYearMonth = currentYearMonth,
            user = this.user
        ).let { newCurrentInvoice ->
            createFixedStatementsToTargetInvoice(newCurrentInvoice)
        }
    }

    private fun handleFutureInvoices(user: UserEntity, diff: Int) {
        val currentInvoice = invoiceRepository.findByUserIdAndState(
            userId = user.id!!,
            state = CURRENT
        ) ?: throw InvoiceNotFoundException("Current invoice for user ${user.id} not found")

        (1..diff).forEach { _ ->
            createFutureInvoice(user).let { newFutureInvoice ->
                currentInvoice.createFixedStatementsToTargetInvoice(
                    targetInvoice = newFutureInvoice
                )
            }
        }
    }

    private fun InvoiceEntity.createFixedStatementsToTargetInvoice(targetInvoice: InvoiceEntity) {
        statementRepository.findAllByInvoiceIdAndType(
            invoiceId = id!!,
            type = FIXED
        ).map { statement ->
            StatementEntity(
                code = statement.code,
                category = statement.category,
                description = statement.description,
                value = statement.value,
                type = statement.type,
                invoice = targetInvoice
            )
        }.let { newStatements ->
            statementRepository.saveAll(newStatements)
            updateInvoice(invoice = targetInvoice.copy(totalValue = newStatements.sumOf { it.value }))
        }
    }

    private fun InvoiceEntity.getNextInvoice() =
        invoiceRepository.findByUserIdAndReferenceMonthAndState(
            userId = this.user.id!!,
            referenceMonth = this.referenceMonth.toYearMonth().plusMonths(1).toString(),
            state = FUTURE
        )

    private fun createNewCurrentInvoice(
        currentYearMonth: YearMonth,
        user: UserEntity
    ) = invoiceRepository.save(
        InvoiceEntity(
            referenceMonth = currentYearMonth.toString(),
            totalValue = 0L,
            state = CURRENT,
            user = user
        )
    )

    private fun InvoiceEntity.sumStatementValues() =
        statementRepository.findAllByInvoiceId(id!!).sumOf { it.value }
}
