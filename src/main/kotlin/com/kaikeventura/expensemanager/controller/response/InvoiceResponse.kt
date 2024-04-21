package com.kaikeventura.expensemanager.controller.response

import com.kaikeventura.expensemanager.entity.InvoiceState
import com.kaikeventura.expensemanager.entity.StatementType
import java.time.LocalDateTime
import java.time.YearMonth

data class InvoiceResponse(
    val referenceMonth: YearMonth,
    val totalValue: Long,
    val state: InvoiceState,
    val statements: List<StatementResponse>
)

data class StatementResponse(
    val code: String,
    val description: String,
    val value: Long,
    val type: StatementType,
    val createdAt: LocalDateTime
)
