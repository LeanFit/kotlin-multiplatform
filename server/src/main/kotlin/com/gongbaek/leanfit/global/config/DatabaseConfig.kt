package com.gongbaek.leanfit.global.config

import com.gongbaek.leanfit.infrastructure.database.table.FcmTokensTable
import com.gongbaek.leanfit.infrastructure.database.table.IdempotencyKeysTable
import com.gongbaek.leanfit.infrastructure.database.table.NotificationsTable
import com.gongbaek.leanfit.infrastructure.database.table.PaymentHistoryTable
import com.gongbaek.leanfit.infrastructure.database.table.RefreshTokensTable
import com.gongbaek.leanfit.infrastructure.database.table.SubscriptionsTable
import com.gongbaek.leanfit.infrastructure.database.table.UserSettingsTable
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
            UserSettingsTable,
            RefreshTokensTable,
            FcmTokensTable,
            SubscriptionsTable,
            PaymentHistoryTable,
            NotificationsTable,
            IdempotencyKeysTable,
        )
    }

    println("Database connected successfully!")
}
