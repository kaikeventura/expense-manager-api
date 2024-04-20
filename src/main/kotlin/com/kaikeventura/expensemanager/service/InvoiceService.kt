package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.common.brazilZoneId
import com.kaikeventura.expensemanager.entity.InvoiceEntity
import com.kaikeventura.expensemanager.entity.InvoiceState.*
import com.kaikeventura.expensemanager.error.exception.InvoiceNotFoundException
import com.kaikeventura.expensemanager.error.exception.UserNotFoundException
import com.kaikeventura.expensemanager.repository.InvoiceRepository
import com.kaikeventura.expensemanager.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class InvoiceService(
    private val userRepository: UserRepository,
    private val invoiceRepository: InvoiceRepository
) {

    fun createFirstInvoiceForAllUsers() {
        userRepository.findAll().forEach {
            invoiceRepository.save(
                InvoiceEntity(
                    referenceMonth = YearMonth.now(brazilZoneId()),
                    totalValue = 0L,
                    state = CURRENT,
                    user = it
                )
            )
        }
    }

    fun createFutureInvoice(
        userId: String
    ): InvoiceEntity =
        userRepository.findByIdOrNull(userId)?.let { user ->
            invoiceRepository.findFirstByUserIdOrderByReferenceMonthDesc(user.id!!)?.let { lastInvoice ->
                invoiceRepository.save(
                    InvoiceEntity(
                        referenceMonth = lastInvoice.referenceMonth.plusMonths(1),
                        totalValue = 0L,
                        state = FUTURE,
                        user = user
                    )
                )
            } ?: throw InvoiceNotFoundException("Last invoice for user $userId not found")
        } ?: throw  UserNotFoundException("User $userId not found")
}
