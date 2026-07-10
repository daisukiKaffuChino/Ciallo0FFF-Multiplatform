package io.github.daisukikaffuchino.ciallo0fff

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform