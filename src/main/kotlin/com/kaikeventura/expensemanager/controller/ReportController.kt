package com.kaikeventura.expensemanager.controller

import com.kaikeventura.expensemanager.configuration.JwtService
import com.kaikeventura.expensemanager.service.ReportService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@RestController
@RequestMapping("/reports")
class ReportController(
    private val jwtService: JwtService,
    private val reportService: ReportService
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{referenceMonth}/categories")
    fun getInvoiceCategoriesReport(
        @RequestHeader("Authorization") token: String,
        @PathVariable("referenceMonth") referenceMonth: YearMonth
    ) = reportService.getInvoiceCategoriesReportByReferenceMonth(
        userEmail = jwtService.extractUsername(token.substring(7)),
        referenceMonth = referenceMonth
    )

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{referenceMonth}/types")
    fun getInvoiceTypesReport(
        @RequestHeader("Authorization") token: String,
        @PathVariable("referenceMonth") referenceMonth: YearMonth
    ) = reportService.getInvoiceTypesReportByReferenceMonth(
        userEmail = jwtService.extractUsername(token.substring(7)),
        referenceMonth = referenceMonth
    )
}
