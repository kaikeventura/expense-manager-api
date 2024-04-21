package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.controller.request.StatementRequest
import com.kaikeventura.expensemanager.entity.InvoiceEntity
import com.kaikeventura.expensemanager.entity.StatementEntity
import com.kaikeventura.expensemanager.entity.StatementType.CREDIT_CARD
import com.kaikeventura.expensemanager.entity.StatementType.FIXED
import com.kaikeventura.expensemanager.error.exception.UserNotFoundException
import com.kaikeventura.expensemanager.repository.StatementRepository
import com.kaikeventura.expensemanager.repository.UserRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN

@Service
class StatementService(
    private val invoiceService: InvoiceService,
    private val userRepository: UserRepository,
    private val statementRepository: StatementRepository
) {

    fun createStatementByProportionality(
        userEmail: String,
        statementRequest: StatementRequest
    ) {
        statementRequest.withProportionality().also { statement ->
            when (statementRequest.type) {
                CREDIT_CARD -> createCreditCardStatement(userEmail, statement)
                FIXED -> createMultipleStatementsForFixedExpense(userEmail, statement)
                else -> createUniqueStatement(userEmail, statement)
            }
        }

        statementRequest.withRemainingProportionality().also { statement ->
            val otherUserEmail = statement.proportionality!!.userEmail
            when (statementRequest.type) {
                CREDIT_CARD -> createCreditCardStatement(otherUserEmail, statement)
                FIXED -> createMultipleStatementsForFixedExpense(otherUserEmail, statement)
                else -> createUniqueStatement(otherUserEmail, statement)
            }
        }
    }

    fun createStatement(
        userEmail: String,
        statementRequest: StatementRequest
    ) {
        when (statementRequest.type) {
            CREDIT_CARD -> createCreditCardStatement(userEmail, statementRequest)
            FIXED -> createMultipleStatementsForFixedExpense(userEmail, statementRequest)
            else -> createUniqueStatement(userEmail, statementRequest)
        }
    }

    private fun createCreditCardStatement(
        userEmail: String,
        statementRequest: StatementRequest
    ) {
        if (statementRequest.installmentAmount == null) {
            createUniqueStatement(userEmail, statementRequest)
        }
        createMultipleStatements(userEmail, statementRequest)
    }

    private fun createUniqueStatement(userEmail: String, statementRequest: StatementRequest) {
        userRepository.findByEmail(userEmail)?.let { user ->
            invoiceService.getInvoiceByUserIdAndReferenceMonth(
                user.id!!,
                statementRequest.referenceMonth
            ).let { invoice ->
                statementRepository.save(
                    StatementEntity(
                        code = statementRequest.code.toString(),
                        description = statementRequest.description,
                        category = statementRequest.category,
                        value = statementRequest.value,
                        type = statementRequest.type,
                        invoice = invoice
                    )
                )
                invoice.recalculateInvoiceValue(statementRequest.value)
            }
        } ?: throw UserNotFoundException("User $userEmail not found")
    }

    private fun createMultipleStatements(userEmail: String, statementRequest: StatementRequest) {
        userRepository.findByEmail(userEmail)?.let { user ->
            invoiceService.checkInvoicesAmount(user, statementRequest)
            val installmentValue =
                BigDecimal(statementRequest.value)
                    .divide(
                        BigDecimal(statementRequest.installmentAmount!!),
                        2,
                        HALF_EVEN
                    ).toLong()

            val invoices = invoiceService.getInvoicesLimitedTo(
                user,
                statementRequest.referenceMonth,
                statementRequest.installmentAmount
            )

            invoices.sortedBy { it.referenceMonth }.forEachIndexed { index, invoice ->
                statementRepository.save(
                    StatementEntity(
                        code = statementRequest.code.toString(),
                        description = normalizeCreditCardDescription(statementRequest, index),
                        category = statementRequest.category,
                        value = installmentValue,
                        type = statementRequest.type,
                        invoice = invoice
                    )
                )
                invoice.recalculateInvoiceValue(installmentValue)
            }
        } ?: throw UserNotFoundException("User $userEmail not found")
    }

    private fun createMultipleStatementsForFixedExpense(userEmail: String, statementRequest: StatementRequest) {
        userRepository.findByEmail(userEmail)?.let { user ->
            val invoices = invoiceService.getAllInvoices(
                user,
                statementRequest.referenceMonth
            )

            invoices.sortedBy { it.referenceMonth }.forEach { invoice ->
                statementRepository.save(
                    StatementEntity(
                        code = statementRequest.code.toString(),
                        category = statementRequest.category,
                        description = statementRequest.description,
                        value = statementRequest.value,
                        type = statementRequest.type,
                        invoice = invoice
                    )
                )
                invoice.recalculateInvoiceValue(statementRequest.value)
            }
        } ?: throw UserNotFoundException("User $userEmail not found")
    }

    private fun normalizeCreditCardDescription(
        statementRequest: StatementRequest,
        index: Int
    ): String {
        if (statementRequest.installmentAmount == 1) {
            return statementRequest.description
        }
        return statementRequest.description.plus(" ${index + 1}/${statementRequest.installmentAmount}")
    }

    private fun InvoiceEntity.recalculateInvoiceValue(statementValue: Long) {
        invoiceService.updateInvoice(
            invoiceEntity = this.copy(
                totalValue = this.totalValue.plus(statementValue)
            )
        )
    }
}
