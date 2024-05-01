package com.kaikeventura.expensemanager.service

import com.kaikeventura.expensemanager.controller.response.UserDetailsResponse
import com.kaikeventura.expensemanager.error.exception.UserNotFoundException
import com.kaikeventura.expensemanager.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getUserDetails(userEmail: String): UserDetailsResponse =
        userRepository.findByEmail(userEmail)?.let {
            UserDetailsResponse(
                name = it.name,
                email = it.email
            )
        } ?: throw UserNotFoundException("User $userEmail not found")

    fun getOtherUsers(userEmail: String): List<UserDetailsResponse> =
        userRepository.findAllByEmailNot(userEmail).map {
            UserDetailsResponse(
                name = it.name,
                email = it.email
            )
        }
}
