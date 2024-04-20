package com.kaikeventura.expensemanager.controller.request

import com.kaikeventura.expensemanager.entity.StatementType
import java.time.YearMonth
import java.util.UUID

data class StatementRequest(
    val code: UUID = UUID.randomUUID(),
    val description: String,
    val value: Long,
    val installmentAmount: Int? = null,
    val type: StatementType,
    val referenceMonth: YearMonth,
)
