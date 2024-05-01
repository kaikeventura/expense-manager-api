package com.kaikeventura.expensemanager.controller

import com.kaikeventura.expensemanager.configuration.JwtService
import com.kaikeventura.expensemanager.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val jwtService: JwtService
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/details")
    fun getUserDetails(
        @RequestHeader("Authorization") token: String
    ) = userService.getUserDetails(
        userEmail = jwtService.extractUsername(token.substring(7))
    )

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/others")
    fun getOthersUsers(
        @RequestHeader("Authorization") token: String
    ) = userService.getOtherUsers(
        userEmail = jwtService.extractUsername(token.substring(7))
    )
}
