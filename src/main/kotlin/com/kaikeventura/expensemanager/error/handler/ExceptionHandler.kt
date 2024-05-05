package com.kaikeventura.expensemanager.error.handler

import com.kaikeventura.expensemanager.error.exception.InvoiceNotFoundException
import com.kaikeventura.expensemanager.error.exception.StatementNotFoundException
import com.kaikeventura.expensemanager.error.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserNotFoundException::class)
    fun exception(ex: UserNotFoundException) =
        ResponseError(ex.message)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvoiceNotFoundException::class)
    fun exception(ex: InvoiceNotFoundException) =
        ResponseError(ex.message)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(StatementNotFoundException::class)
    fun exception(ex: StatementNotFoundException) =
        ResponseError(ex.message)

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UsernameNotFoundException::class)
    fun exception(ex: UsernameNotFoundException) =
        ResponseError(ex.message ?: "Unexpected error")

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception) =
        ResponseError(ex.message ?: "Unexpected error")
}

data class ResponseError(
    val error: String
)
