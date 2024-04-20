package com.kaikeventura.expensemanager.controller

import com.kaikeventura.expensemanager.service.InvoiceService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/invoices")
class InvoiceController(
    private val invoiceService: InvoiceService
) {

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/first")
    fun createAccess() =
        invoiceService.createFirstInvoiceForAllUsers()
}
