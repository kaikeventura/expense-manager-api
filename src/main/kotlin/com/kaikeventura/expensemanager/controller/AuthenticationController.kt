package com.kaikeventura.expensemanager.controller

import com.kaikeventura.expensemanager.configuration.JwtService
import com.kaikeventura.expensemanager.controller.request.AuthenticationRequest
import com.kaikeventura.expensemanager.controller.request.RegisterRequest
import com.kaikeventura.expensemanager.controller.response.AuthenticationResponse
import com.kaikeventura.expensemanager.service.AuthenticationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class AuthenticationController(
    private val authenticationService: AuthenticationService,
    private val jwtService: JwtService
) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: RegisterRequest): ResponseEntity<AuthenticationResponse> =
        ResponseEntity.ok(authenticationService.register(request))

    @PostMapping("/sign-on")
    fun signOn(@RequestBody request: AuthenticationRequest): ResponseEntity<AuthenticationResponse> =
        ResponseEntity.ok(authenticationService.login(request))

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/details")
    fun getUserDetails(
        @RequestHeader("Authorization") token: String
    ) = authenticationService.getUserDetails(
        userEmail = jwtService.extractUsername(token.substring(7))
    )

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/others")
    fun getOthersUsers(
        @RequestHeader("Authorization") token: String
    ) = authenticationService.getOtherUsers(
        userEmail = jwtService.extractUsername(token.substring(7))
    )
}
