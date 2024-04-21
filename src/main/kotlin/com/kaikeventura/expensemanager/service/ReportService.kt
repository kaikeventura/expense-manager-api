package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.controller.response.InvoiceCategoriesReportResponse
import com.kaikeventura.expensemanager.controller.response.InvoiceTypesReportResponse
import com.kaikeventura.expensemanager.controller.response.StatementCategoriesReportResponse
import com.kaikeventura.expensemanager.controller.response.StatementTypesReportResponse
import com.kaikeventura.expensemanager.error.exception.InvoiceNotFoundException
import com.kaikeventura.expensemanager.repository.InvoiceRepository
import com.kaikeventura.expensemanager.repository.StatementRepository
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class ReportService(
    private val invoiceRepository: InvoiceRepository,
    private val statementRepository: StatementRepository
) {
    fun getInvoiceCategoriesReportByReferenceMonth(userEmail: String, referenceMonth: YearMonth): InvoiceCategoriesReportResponse =
        invoiceRepository.findByUserEmailAndReferenceMonth(userEmail, referenceMonth.toString())?.let { invoice ->
            InvoiceCategoriesReportResponse(
                referenceMonth = YearMonth.parse(invoice.referenceMonth),
                totalValue = invoice.totalValue,
                state = invoice.state,
                statements = statementRepository.findAllByInvoiceId(invoice.id!!)
                    .groupBy {
                        it.category
                    }.map { (category, value) ->
                        StatementCategoriesReportResponse(
                            category = category,
                            totalValue = value.sumOf { it.value }
                        )
                    }
            )
        } ?: throw InvoiceNotFoundException("Last invoice for user $userEmail and reference month $referenceMonth not found")

    fun getInvoiceTypesReportByReferenceMonth(userEmail: String, referenceMonth: YearMonth): InvoiceTypesReportResponse =
        invoiceRepository.findByUserEmailAndReferenceMonth(userEmail, referenceMonth.toString())?.let { invoice ->
            InvoiceTypesReportResponse(
                referenceMonth = YearMonth.parse(invoice.referenceMonth),
                totalValue = invoice.totalValue,
                state = invoice.state,
                types = statementRepository.findAllByInvoiceId(invoice.id!!)
                    .groupBy {
                        it.type
                    }.map { (type, value) ->
                        StatementTypesReportResponse(
                            category = type,
                            totalValue = value.sumOf { it.value }
                        )
                    }
            )
        } ?: throw InvoiceNotFoundException("Last invoice for user $userEmail and reference month $referenceMonth not found")
}
