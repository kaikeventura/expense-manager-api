package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.configuration.JwtService
import com.kaikeventura.expensemanager.controller.request.AuthenticationRequest
import com.kaikeventura.expensemanager.controller.request.RegisterRequest
import com.kaikeventura.expensemanager.controller.response.AuthenticationResponse
import com.kaikeventura.expensemanager.entity.Role
import com.kaikeventura.expensemanager.entity.UserEntity
import com.kaikeventura.expensemanager.error.exception.UserNotFoundException
import com.kaikeventura.expensemanager.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
) {

    fun register(request: RegisterRequest): AuthenticationResponse =
        request.let {
            UserEntity(
                name = it.username,
                email = it.email,
                pass = passwordEncoder.encode(it.password),
                role = Role.USER
            )
        }.let {
            userRepository.save(it)
        }.let {
            AuthenticationResponse(
                token = jwtService.generateToken(it)
            )
        }

    fun login(request: AuthenticationRequest): AuthenticationResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )
        val user = userRepository.findByEmail(request.email) ?: throw UserNotFoundException("User $request.email not found")

        return AuthenticationResponse(
            token = jwtService.generateToken(user)
        )
    }
}
