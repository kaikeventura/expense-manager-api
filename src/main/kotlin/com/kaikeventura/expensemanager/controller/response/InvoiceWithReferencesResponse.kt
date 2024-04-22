package com.kaikeventura.expensemanager.controller.response

import com.kaikeventura.expensemanager.entity.InvoiceState

data class InvoiceWithReferencesResponse(
    val referenceMonth: String,
    val state: InvoiceState
)
