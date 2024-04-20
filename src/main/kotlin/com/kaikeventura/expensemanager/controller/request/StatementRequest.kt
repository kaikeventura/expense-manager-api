package com.kaikeventura.expensemanager.controller.request

import com.kaikeventura.expensemanager.entity.StatementType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import java.util.UUID

data class StatementRequest(
    val code: UUID = UUID.randomUUID(),
    val description: String,
    val value: Long,
    val installmentAmount: Int? = null,
    val type: StatementType,
    val referenceMonth: YearMonth,
    val proportionality: Proportionality? = null
) {
    fun withProportionality(): StatementRequest {
        val percentage = proportionality!!.percentage
        val proportionalityValue =
            BigDecimal(value)
                .multiply(percentage)
                .divide(
                    BigDecimal(100),
                    2,
                    RoundingMode.HALF_EVEN
                ).toLong()

        return this.copy(
            value = proportionalityValue
        )
    }

    fun withRemainingProportionality(): StatementRequest {
        val percentage = BigDecimal(100).minus(proportionality!!.percentage)
        val proportionalityValue =
            BigDecimal(value)
                .multiply(percentage)
                .divide(
                    BigDecimal(100),
                    2,
                    RoundingMode.HALF_EVEN
                ).toLong()

        return this.copy(
            value = proportionalityValue
        )
    }
}

data class Proportionality(
    val userEmail: String,
    val percentage: BigDecimal
)
