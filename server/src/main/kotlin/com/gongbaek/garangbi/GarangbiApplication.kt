package com.gongbaek.garangbi

import com.gongbaek.garangbi.global.config.AppConfig
import com.gongbaek.garangbi.global.config.configureDatabase
import com.gongbaek.garangbi.global.config.configureHTTP
import com.gongbaek.garangbi.global.config.configureMonitoring
import com.gongbaek.garangbi.global.config.configureRouting
import com.gongbaek.garangbi.global.config.configureSerialization
import com.gongbaek.garangbi.global.exception.configureExceptionHandling
import com.gongbaek.garangbi.global.security.configureSecurity
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    AppConfig.init(environment.config)
    configureDatabase()
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureMonitoring()
    configureExceptionHandling()
    configureRouting()
}
