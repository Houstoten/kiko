package com.kiko

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kiko.flat.FlatService
import com.kiko.flat.dto.RequestViewingDto
import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.InternalServerErrorResponse
import java.lang.Exception

fun main() {
    val config = Configuration.readConfiguration();
    val app = Javalin.create().start(config[ApplicationConfiguration.port])
    app.config.defaultContentType = "application/json"
    app.post("/request") { ctx ->
        ctx.status(200)
        toJsonOrThrow(ctx) { FlatService.requestViewing(ctx.body<RequestViewingDto>()) }
    }
    app.patch("/cancel") { ctx ->
        ctx.status(204)
        toJsonOrThrow(ctx) { FlatService.cancelViewing(ctx.body<RequestViewingDto>()) }
    }
}

private fun toJsonOrThrow(ctx: Context, function: () -> Any) =
    runCatching { function() }
        .fold(
            {
                ctx.json(it)
            }, {
                when (it) {
                    is Exception -> throw BadRequestResponse(it.localizedMessage)
                    else -> throw InternalServerErrorResponse(it.localizedMessage)
                }
            }
        )
