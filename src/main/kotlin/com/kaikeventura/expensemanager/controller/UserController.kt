package com.kaikeventura.expensemanager.controller

import com.kaikeventura.expensemanager.controller.request.AuthenticationRequest
import com.kaikeventura.expensemanager.controller.request.RegisterRequest
import com.kaikeventura.expensemanager.controller.response.AuthenticationResponse
import com.kaikeventura.expensemanager.service.AuthenticationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val authenticationService: AuthenticationService
) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: RegisterRequest): ResponseEntity<AuthenticationResponse> =
        ResponseEntity.ok(authenticationService.register(request))

    @PostMapping("/sign-on")
    fun signOn(@RequestBody request: AuthenticationRequest): ResponseEntity<AuthenticationResponse> =
        ResponseEntity.ok(authenticationService.login(request))
}
