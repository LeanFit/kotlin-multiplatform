package com.gongbaek.garangbi.global.config

import io.ktor.server.config.ApplicationConfig

object AppConfig {
    private lateinit var config: ApplicationConfig

    fun init(applicationConfig: ApplicationConfig) {
        config = applicationConfig
    }

    val isDevelopment: Boolean
        get() = System.getenv("KTOR_ENV") != "production"

    val serverPort: Int
        get() = config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull() ?: 8080

    object Jwt {
        val secret: String
            get() = config.property("jwt.secret").getString()

        val issuer: String
            get() = config.property("jwt.issuer").getString()

        val audience: String
            get() = config.property("jwt.audience").getString()

        val realm: String
            get() = config.property("jwt.realm").getString()

        val expirationDays: Long = 30
    }

    object Database {
        val url: String
            get() = config.property("database.url").getString()

        val user: String
            get() = config.property("database.user").getString()

        val password: String
            get() = config.property("database.password").getString()

        val driverClassName: String = "org.postgresql.Driver"

        val maxPoolSize: Int
            get() = config.propertyOrNull("database.poolSize")?.getString()?.toIntOrNull() ?: 10
    }
}
