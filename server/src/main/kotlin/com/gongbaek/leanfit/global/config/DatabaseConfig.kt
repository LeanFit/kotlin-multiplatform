package com.gongbaek.leanfit.global.config

import com.gongbaek.leanfit.infrastructure.database.table.SubscriptionsTable
import com.gongbaek.leanfit.infrastructure.database.table.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val config =
        HikariConfig().apply {
            jdbcUrl = AppConfig.Database.url
            username = AppConfig.Database.user
            password = AppConfig.Database.password
            driverClassName = AppConfig.Database.driverClassName
            maximumPoolSize = AppConfig.Database.maxPoolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(
            UsersTable,
            SubscriptionsTable,
        )
    }

    println("Database connected successfully!")
}
