package com.kaikeventura.expensemanager.error.exception

data class UserNotFoundException(
    override val message: String
) : RuntimeException(message)
