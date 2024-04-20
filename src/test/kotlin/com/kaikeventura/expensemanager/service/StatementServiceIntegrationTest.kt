package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.configuration.TestContainersConfiguration
import com.kaikeventura.expensemanager.controller.request.StatementRequest
import com.kaikeventura.expensemanager.entity.Role
import com.kaikeventura.expensemanager.entity.StatementType.*
import com.kaikeventura.expensemanager.entity.UserEntity
import com.kaikeventura.expensemanager.repository.InvoiceRepository
import com.kaikeventura.expensemanager.repository.StatementRepository
import com.kaikeventura.expensemanager.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StatementServiceIntegrationTest : TestContainersConfiguration() {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var invoiceRepository: InvoiceRepository

    @Autowired
    private lateinit var statementRepository: StatementRepository

    @Autowired
    private lateinit var invoiceService: InvoiceService

    @Autowired
    private lateinit var statementService: StatementService

    @AfterEach
    fun down() {
        statementRepository.deleteAll()
        invoiceRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `it should create a in cash expense`() {
        val user = anUser("anyoneuser@gmail.com")
        invoiceService.createFirstInvoiceForAllUsers()

        val invoice = invoiceRepository.findAll().single()
        statementService.createStatement(
            userEmail = user.email,
            statementRequest = StatementRequest(
                description = "Watter bill",
                value = 150_00L,
                type = IN_CASH,
                referenceMonth = invoice.referenceMonth
            )
        )

        invoiceRepository.findAll().single { it.user.id == user.id }.let {
            assertEquals(150_00L, it.totalValue)
        }

        statementRepository.findAllByInvoiceId(invoice.id!!).single().let {
            assertEquals("Watter bill", it.description)
            assertEquals(IN_CASH, it.type)
            assertEquals(150_00L, it.value)
        }
    }

    @Test
    fun `it should create a credit card expense with one installment`() {
        val user = anUser("anytwouser@gmail.com")
        invoiceService.createFirstInvoiceForAllUsers()

        val invoice = invoiceRepository.findAll().single()
        statementService.createStatement(
            userEmail = user.email,
            statementRequest = StatementRequest(
                description = "Amazon",
                value = 150_00L,
                installmentAmount = 1,
                type = CREDIT_CARD,
                referenceMonth = invoice.referenceMonth
            )
        )

        invoiceRepository.findAll().single { it.user.id == user.id }.let {
            assertEquals(150_00L, it.totalValue)
        }

        statementRepository.findAllByInvoiceId(invoice.id!!).single().let {
            assertEquals("Amazon", it.description)
            assertEquals(CREDIT_CARD, it.type)
            assertEquals(150_00L, it.value)
        }
    }

    @Test
    fun `it should create a credit card expense with multiple installments`() {
        val user = anUser("anythreeuser@gmail.com")
        invoiceService.createFirstInvoiceForAllUsers()

        val invoice = invoiceRepository.findAll().single()
        statementService.createStatement(
            userEmail = user.email,
            statementRequest = StatementRequest(
                description = "Amazon",
                value = 300_30L,
                installmentAmount = 3,
                type = CREDIT_CARD,
                referenceMonth = invoice.referenceMonth
            )
        )

        invoiceRepository.findAll().filter { it.user.id == user.id }.also {
            assertEquals(it.size, 3)
        }.forEach {
            assertEquals(100_10L, it.totalValue)
        }

        statementRepository.findAllByInvoiceUserId(user.id!!).also {
            assertEquals(it.size, 3)
        }.sortedBy {
            it.createdAt
        }.forEachIndexed { index, statement ->
            assertEquals("Amazon ${index + 1}/3", statement.description)
            assertEquals(CREDIT_CARD, statement.type)
            assertEquals(100_10, statement.value)
        }
    }

    @Test
    fun `it should create a fixed expense in all invoices`() {
        val user = anUser("anyfouruser@gmail.com")
        invoiceService.createFirstInvoiceForAllUsers()

        repeat(3) {
            invoiceService.createFutureInvoice(user)
        }

        val invoice = invoiceRepository.findAll().minByOrNull { it.referenceMonth }!!
        statementService.createStatement(
            userEmail = user.email,
            statementRequest = StatementRequest(
                description = "House financing",
                value = 2000_00L,
                type = FIXED,
                referenceMonth = invoice.referenceMonth
            )
        )

        invoiceRepository.findAll().filter { it.user.id == user.id }.also {
            assertEquals(it.size, 4)
        }.forEach {
            assertEquals(2000_00L, it.totalValue)
        }

        statementRepository.findAllByInvoiceUserId(user.id!!).also {
            assertEquals(it.size, 4)
        }.sortedBy {
            it.createdAt
        }.forEach { statement ->
            assertEquals("House financing", statement.description)
            assertEquals(FIXED, statement.type)
            assertEquals(2000_00L, statement.value)
        }
    }

    private fun anUser(email: String): UserEntity =
        userRepository.save(
            UserEntity(
                name = "Any User",
                email = email,
                pass = "123",
                role = Role.USER
            )
        )
}
