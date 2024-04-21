package com.kaikeventura.expensemanager.controller.response

import com.kaikeventura.expensemanager.entity.InvoiceState
import java.time.YearMonth

data class InvoiceWithReferencesResponse(
    val referenceMonth: YearMonth,
    val state: InvoiceState
)
