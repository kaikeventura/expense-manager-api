package com.kaikeventura.expensemanager.entity

interface InvoiceWithReferenceProjection {
    val referenceMonth: String
    val state: InvoiceState
}
