package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.controller.request.StatementRequest
import com.kaikeventura.expensemanager.entity.InvoiceEntity
import com.kaikeventura.expensemanager.entity.StatementEntity
import com.kaikeventura.expensemanager.entity.StatementType.CREDIT_CARD
import com.kaikeventura.expensemanager.error.exception.UserNotFoundException
import com.kaikeventura.expensemanager.repository.StatementRepository
import com.kaikeventura.expensemanager.repository.UserRepository
import org.springframework.stereotype.Service

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
        if (statementRequest.type == CREDIT_CARD) {
            createCreditCardStatement(userEmail, statementRequest)
        }

        createUniqueStatement(userEmail, statementRequest)
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
                invoice.updateInvoiceAmount(statementRequest)
            }
        } ?: throw UserNotFoundException("User $userEmail not found")
    }

    private fun InvoiceEntity.updateInvoiceAmount(
        statementRequest: StatementRequest
    ) {
        invoiceService.updateInvoiceTotalAmount(
            invoiceEntity = this,
            newStatementValue = statementRequest.value
        )
    }

    private fun createMultipleStatements(userEmail: String, statementRequest: StatementRequest) {

    }
}
