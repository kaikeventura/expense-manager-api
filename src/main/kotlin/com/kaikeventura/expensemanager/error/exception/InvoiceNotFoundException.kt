package com.kaikeventura.expensemanager.error.exception

data class InvoiceNotFoundException(
    override val message: String
) : RuntimeException(message)
