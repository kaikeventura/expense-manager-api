package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.common.brazilZoneId
import com.kaikeventura.expensemanager.common.toYearMonth
import com.kaikeventura.expensemanager.configuration.TestContainersConfiguration
import com.kaikeventura.expensemanager.controller.request.StatementRequest
import com.kaikeventura.expensemanager.entity.InvoiceState.*
import com.kaikeventura.expensemanager.entity.Role
import com.kaikeventura.expensemanager.entity.StatementCategory.HOUSING
import com.kaikeventura.expensemanager.entity.StatementType.FIXED
import com.kaikeventura.expensemanager.entity.UserEntity
import com.kaikeventura.expensemanager.repository.InvoiceRepository
import com.kaikeventura.expensemanager.repository.StatementRepository
import com.kaikeventura.expensemanager.repository.UserRepository
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.YearMonth

@SpringBootTest
class InvoiceServiceIntegrationTest : TestContainersConfiguration() {

    @Autowired
    private lateinit var invoiceRepository: InvoiceRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var invoiceService: InvoiceService

    @Autowired
    private lateinit var statementService: StatementService

    @Autowired
    private lateinit var statementRepository: StatementRepository

        @AfterEach
    fun down() {
        statementRepository.deleteAll()
        invoiceRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `it should create a first invoice for all users`() {
        val anUser = anUser("dunhatcho@gmail.com")
        invoiceService.createFirstInvoiceForAllUsers()

        repeat(3) {
            invoiceService.createFirstInvoiceForAllUsers()
        }

        assertEquals(anUser.id, invoiceRepository.findAllByUserId(anUser.id!!)[0].user.id)
    }

    @Test
    fun `it should create a next invoice for user`() {
        val anUser = anUser("dunheco@gmail.com")
        invoiceService.createFirstInvoiceForAllUsers()

        val firstInvoice = invoiceRepository.findAllByUserId(anUser.id!!).single()
        val futureInvoice = invoiceService.createFutureInvoice(anUser)

        val expectedNextReferenceMonth = YearMonth.parse(firstInvoice.referenceMonth).plusMonths(1)

        assertEquals(expectedNextReferenceMonth.toString(), futureInvoice.referenceMonth)
    }

    @Test
    fun `it should change current invoice state to previous and create new current invoice without future invoice`() {
        val anUser = anUser("dunhinha@gmail.com")

        mockkStatic(YearMonth::class)
        every { YearMonth.now(brazilZoneId()) } returns YearMonth.parse("2024-04")

        invoiceService.createFirstInvoiceForAllUsers()

        every { YearMonth.now(brazilZoneId()) } returns YearMonth.parse("2024-05")

        invoiceService.handleInvoiceCycles()

        unmockkStatic(YearMonth::class)

        val invoices = invoiceRepository.findAllByUserId(anUser.id!!)

        with(invoices.single { it.state == PREVIOUS }) {
            assertEquals(YearMonth.parse("2024-04"), referenceMonth.toYearMonth())
        }

        with(invoices.single { it.state == CURRENT }) {
            assertEquals(YearMonth.parse("2024-05"), referenceMonth.toYearMonth())
        }

        assertTrue(invoices.none { it.state == FUTURE })
    }

    @Test
    fun `it should change current invoice state to previous and change next future invoice to current without future invoice`() {
        val anUser = anUser("dunhinha@gmail.com")

        mockkStatic(YearMonth::class)
        every { YearMonth.now(brazilZoneId()) } returns YearMonth.parse("2024-04")

        invoiceService.createFirstInvoiceForAllUsers()
        invoiceService.createFutureInvoice(anUser)

        every { YearMonth.now(brazilZoneId()) } returns YearMonth.parse("2024-05")

        val futureInvoice = invoiceRepository.findAllByUserId(anUser.id!!).single { it.state == FUTURE }

        invoiceService.handleInvoiceCycles()

        unmockkStatic(YearMonth::class)

        val invoices = invoiceRepository.findAllByUserId(anUser.id!!)

        with(invoices.single { it.state == PREVIOUS }) {
            assertEquals(YearMonth.parse("2024-04"), referenceMonth.toYearMonth())
        }

        with(invoices.single { it.state == CURRENT }) {
            assertEquals(YearMonth.parse("2024-05"), referenceMonth.toYearMonth())
            assertEquals(futureInvoice.id, id)
        }

        assertTrue(invoices.none { it.state == FUTURE })
    }

    @Test
    fun `it should change current invoice state to previous and change next future invoice to current with future invoice`() {
        val anUser = anUser("dunhinha@gmail.com")

        mockkStatic(YearMonth::class)
        every { YearMonth.now(brazilZoneId()) } returns YearMonth.parse("2024-04")

        invoiceService.createFirstInvoiceForAllUsers()

        repeat(2) {
            invoiceService.createFutureInvoice(anUser)
        }

        every { YearMonth.now(brazilZoneId()) } returns YearMonth.parse("2024-05")

        val previousFutureInvoice = invoiceRepository.findAllByUserId(anUser.id!!).first {
            it.referenceMonth == YearMonth.parse("2024-05").toString()
        }

        invoiceService.handleInvoiceCycles()

        unmockkStatic(YearMonth::class)

        val invoices = invoiceRepository.findAllByUserId(anUser.id!!)

        with(invoices.single { it.state == PREVIOUS }) {
            assertEquals(YearMonth.parse("2024-04"), referenceMonth.toYearMonth())
        }

        with(invoices.single { it.state == CURRENT }) {
            assertEquals(YearMonth.parse("2024-05"), referenceMonth.toYearMonth())
            assertEquals(previousFutureInvoice.id, id)
        }

        with(invoices.single { it.state == FUTURE }) {
            assertEquals(YearMonth.parse("2024-06"), referenceMonth.toYearMonth())
        }
    }

    @Test
    fun `it should change current invoice state to previous, create new current invoice without future invoice and move fixed statements`() {
        val anUser = anUser("dunhinha@gmail.com")

        mockkStatic(YearMonth::class)
        every { YearMonth.now(brazilZoneId()) } returns YearMonth.parse("2024-04")

        invoiceService.createFirstInvoiceForAllUsers()

        statementService.createStatement(
            userEmail = anUser.email,
            statementRequest = StatementRequest(
                description = "House financing",
                category = HOUSING,
                value = 2000_00L,
                type = FIXED,
                referenceMonth = YearMonth.parse(YearMonth.parse("2024-04").toString())
            )
        )

        every { YearMonth.now(brazilZoneId()) } returns YearMonth.parse("2024-05")

        invoiceService.handleInvoiceCycles()

        unmockkStatic(YearMonth::class)

        val invoices = invoiceRepository.findAllByUserId(anUser.id!!)

        with(invoices.single { it.state == PREVIOUS }) {
            assertEquals(YearMonth.parse("2024-04"), referenceMonth.toYearMonth())
        }

        with(invoices.single { it.state == CURRENT }) {
            assertEquals(YearMonth.parse("2024-05"), referenceMonth.toYearMonth())
            assertEquals(2000_00L, totalValue)
            with(statementRepository.findAllByInvoiceId(id!!)) {
                assertEquals(1, size)
                assertEquals(2000_00L, single { it.type == FIXED }.value)
            }
        }

        assertTrue(invoices.none { it.state == FUTURE })
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
