package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.configuration.TestContainersConfiguration
import com.kaikeventura.expensemanager.controller.request.RegisterRequest
import com.kaikeventura.expensemanager.entity.UserEntity
import com.kaikeventura.expensemanager.repository.InvoiceRepository
import com.kaikeventura.expensemanager.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InvoiceServiceIntegrationTest : TestContainersConfiguration() {

    @Autowired
    private lateinit var invoiceRepository: InvoiceRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var invoiceService: InvoiceService

    @Autowired
    private lateinit var authenticationService: AuthenticationService

    private lateinit var user: UserEntity

    @BeforeEach
    fun setup() {
        authenticationService.register(
            request = RegisterRequest(
                username = "Donnie",
                email = "donnie@gmail.com",
                password = "123"
            )
        )

        invoiceService.createFirstInvoiceForAllUsers()

        user = userRepository.findAll().single()
    }

    @AfterEach
    fun down() {
        invoiceRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `it should create a first invoice for all users`() {
        invoiceRepository.findAll().first().let {
            assertEquals(user.id, it.user.id)
        }
    }

    @Test
    fun `it should create a next invoice for user`() {
        val firstInvoice = invoiceRepository.findAll().first()
        val futureInvoice = invoiceService.createFutureInvoice(user)

        val expectedNextReferenceMonth = firstInvoice.referenceMonth.plusMonths(1)

        assertEquals(expectedNextReferenceMonth, futureInvoice.referenceMonth)
    }
}
