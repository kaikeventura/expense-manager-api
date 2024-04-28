package com.kaikeventura.expensemanager.common

import java.time.YearMonth
import java.time.ZoneId

fun brazilZoneId(): ZoneId = ZoneId.of("America/Sao_Paulo")

fun String.toYearMonth() = YearMonth.parse(this)
