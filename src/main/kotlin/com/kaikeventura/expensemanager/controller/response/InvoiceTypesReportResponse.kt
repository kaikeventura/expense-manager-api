package com.kaikeventura.expensemanager.controller.response

import com.kaikeventura.expensemanager.entity.InvoiceState
import com.kaikeventura.expensemanager.entity.StatementType
import java.time.YearMonth

data class InvoiceTypesReportResponse(
    val referenceMonth: YearMonth,
    val totalValue: Long,
    val state: InvoiceState,
    val types: List<StatementTypesReportResponse>
)

data class StatementTypesReportResponse(
    val category: StatementType,
    val totalValue: Long
)
