package com.kiko

import io.javalin.Javalin

fun main() {
    val config = Configuration.readConfiguration();
    val app = Javalin.create().start(config[ApplicationConfiguration.port])
    app.get("/") { ctx -> ctx.result("Hello World") }
}
