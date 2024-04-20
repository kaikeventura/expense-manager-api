package com.kaikeventura.expensemanager.configuration

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
open class TestContainersConfiguration {

    companion object {

        @JvmStatic
        private val MY_SQL_CONTAINER: MySQLContainer<*> = MySQLContainer(
            DockerImageName.parse("mysql").withTag("latest")
        ).withReuse(true)

        @JvmStatic
        @DynamicPropertySource
        private fun mysqlProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { MY_SQL_CONTAINER.jdbcUrl }
            registry.add("spring.datasource.username") { MY_SQL_CONTAINER.username }
            registry.add("spring.datasource.password") { MY_SQL_CONTAINER.password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create" }
        }

        init {
            MY_SQL_CONTAINER.start()
        }
    }
}
