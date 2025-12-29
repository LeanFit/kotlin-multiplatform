package com.gongbaek.leanfit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
