package com.kaikeventura.expensemanager.repository

import com.kaikeventura.expensemanager.entity.StatementEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StatementRepository : JpaRepository<StatementEntity, String>