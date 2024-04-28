package com.kaikeventura.expensemanager.controller

import com.kaikeventura.expensemanager.configuration.JwtService
import com.kaikeventura.expensemanager.service.InvoiceService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@RestController
@RequestMapping("/invoices")
class InvoiceController(
    private val jwtService: JwtService,
    private val invoiceService: InvoiceService
) {

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/first")
    fun createAccess() =
        invoiceService.createFirstInvoiceForAllUsers()

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/references")
    fun getInvoiceWithReferences(
        @RequestHeader("Authorization") token: String
    ) = invoiceService.getInvoiceWithReferences(
        userEmail = jwtService.extractUsername(token.substring(7))
    )

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{referenceMonth}")
    fun getInvoiceByYearMonth(
        @RequestHeader("Authorization") token: String,
        @PathVariable("referenceMonth") referenceMonth: YearMonth
    ) = invoiceService.getInvoiceByReferenceMonth(
        userEmail = jwtService.extractUsername(token.substring(7)),
        referenceMonth = referenceMonth
    )

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/cycles")
    fun getInvoiceByYearMonth() = invoiceService.handleInvoiceCycles()
}
