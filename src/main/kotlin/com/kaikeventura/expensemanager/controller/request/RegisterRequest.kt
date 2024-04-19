package com.kaikeventura.expensemanager.controller.request

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)
