package com.kaikeventura.expensemanager.controller.request

data class AuthenticationRequest(
    val email: String,
    val password: String
)
