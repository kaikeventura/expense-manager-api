package com.kaikeventura.expensemanager.controller.response

import com.kaikeventura.expensemanager.entity.InvoiceState
import com.kaikeventura.expensemanager.entity.StatementCategory
import java.time.YearMonth

data class InvoiceCategoriesReportResponse(
    val referenceMonth: YearMonth,
    val totalValue: Long,
    val state: InvoiceState,
    val statements: List<StatementCategoriesReportResponse>
)

data class StatementCategoriesReportResponse(
    val category: StatementCategory,
    val totalValue: Long
)
