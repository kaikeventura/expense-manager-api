package com.kaikeventura.expensemanager.controller.request

import com.kaikeventura.expensemanager.entity.StatementType
import java.time.YearMonth

data class StatementRequest(
    val description: String,
    val value: Long,
    val installmentAmount: Int? = null,
    val type: StatementType,
    val referenceMonth: YearMonth,
)
