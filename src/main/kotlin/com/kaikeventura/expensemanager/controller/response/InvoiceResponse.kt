package com.kaikeventura.expensemanager.controller.response

import com.kaikeventura.expensemanager.entity.InvoiceState
import com.kaikeventura.expensemanager.entity.StatementCategory
import com.kaikeventura.expensemanager.entity.StatementType

data class InvoiceResponse(
    val referenceMonth: String,
    val totalValue: Long,
    val state: InvoiceState,
    val statements: List<StatementResponse>
)

data class StatementResponse(
    val code: String,
    val description: String,
    val category: StatementCategory,
    val value: Long,
    val type: StatementType,
    val createdAt: String
)
