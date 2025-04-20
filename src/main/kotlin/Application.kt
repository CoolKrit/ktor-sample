package com.apppillar

import com.apppillar.database.DatabaseFactory
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()

    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
