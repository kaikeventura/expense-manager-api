package com.kaikeventura.expensemanager.error.exception

data class StatementNotFoundException(
    override val message: String
) : RuntimeException(message)
