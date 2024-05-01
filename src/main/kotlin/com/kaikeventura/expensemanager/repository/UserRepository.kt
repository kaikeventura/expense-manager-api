package com.kaikeventura.expensemanager.repository

import com.kaikeventura.expensemanager.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, String> {
    fun findByEmail(email: String): UserEntity?

    @Query(value = "SELECT u.* FROM users u LEFT JOIN invoices i ON u.id = i.user_id WHERE i.id IS NULL", nativeQuery = true)
    fun findAllWithoutInvoice(): List<UserEntity>

    fun findAllByEmailNot(userEmail: String): List<UserEntity>
}
