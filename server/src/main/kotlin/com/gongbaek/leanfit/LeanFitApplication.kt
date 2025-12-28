package com.gongbaek.leanfit

import com.gongbaek.leanfit.global.config.AppConfig
import com.gongbaek.leanfit.global.config.configureDatabase
import com.gongbaek.leanfit.global.config.configureHTTP
import com.gongbaek.leanfit.global.config.configureMonitoring
import com.gongbaek.leanfit.global.config.configureRouting
import com.gongbaek.leanfit.global.config.configureSerialization
import com.gongbaek.leanfit.global.exception.configureExceptionHandling
import com.gongbaek.leanfit.global.security.configureSecurity
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
