package com.kaikeventura.expensemanager

import com.kaikeventura.expensemanager.service.InvoiceService
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class ExpenseManagerApplication

fun main(args: Array<String>) {
	runApplication<ExpenseManagerApplication>(*args)
}

@Component
class Init(
	private val invoiceService: InvoiceService
) {

	@PostConstruct
	fun handleInvoiceCycles() {
		invoiceService.handleInvoiceCycles()
	}
}
