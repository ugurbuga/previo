package com.ugurbuga.previo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform