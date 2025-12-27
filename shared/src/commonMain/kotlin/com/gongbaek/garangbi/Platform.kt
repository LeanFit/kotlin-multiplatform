package com.gongbaek.garangbi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform