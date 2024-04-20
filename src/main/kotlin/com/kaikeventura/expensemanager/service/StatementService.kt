package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.controller.request.StatementRequest
import com.kaikeventura.expensemanager.entity.InvoiceEntity
import com.kaikeventura.expensemanager.entity.StatementEntity
import com.kaikeventura.expensemanager.entity.StatementType.CREDIT_CARD
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

    fun createStatement(
        userEmail: String,
        statementRequest: StatementRequest
    ) {
        if (statementRequest.type != CREDIT_CARD) {
            createUniqueStatement(userEmail, statementRequest)
        }

        createCreditCardStatement(userEmail, statementRequest)
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
                        description = statementRequest.description.plus(" ${index + 1}/${statementRequest.installmentAmount}"),
                        value = installmentValue,
                        type = statementRequest.type,
                        invoice = invoice
                    )
                )
                invoice.recalculateInvoiceValue(installmentValue)
            }
        } ?: throw UserNotFoundException("User $userEmail not found")
    }

    private fun InvoiceEntity.recalculateInvoiceValue(statementValue: Long) {
        invoiceService.updateInvoice(
            invoiceEntity = this.copy(
                totalValue = this.totalValue.plus(statementValue)
            )
        )
    }
}
