package com.kaikeventura.expensemanager.controller

import com.kaikeventura.expensemanager.configuration.JwtService
import com.kaikeventura.expensemanager.controller.request.StatementRequest
import com.kaikeventura.expensemanager.service.StatementService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/statements")
class StatementController(
    private val jwtService: JwtService,
    private val statementService: StatementService
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createStatement(
        @RequestHeader("Authorization") token: String,
        @RequestBody statementRequest: StatementRequest
    ) = statementService.createStatement(
        userEmail = jwtService.extractUsername(token.substring(7)),
        statementRequest = statementRequest
    )

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/proportionality")
    fun createProportionalityStatement(
        @RequestHeader("Authorization") token: String,
        @RequestBody statementRequest: StatementRequest
    ) = statementService.createStatementByProportionality(
        userEmail = jwtService.extractUsername(token.substring(7)),
        statementRequest = statementRequest
    )
}
