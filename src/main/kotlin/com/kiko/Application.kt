package com.kiko

import com.kiko.flat.FlatService
import com.kiko.flat.dto.ApproveRejectViewingDto
import com.kiko.flat.dto.RequestCancelViewingDto
import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.InternalServerErrorResponse
import java.lang.Exception

fun main() {
    val config = Configuration.readConfiguration()
    init(config[ApplicationConfiguration.port])
}

fun init(port: Int): Javalin {
    val app = Javalin.create().start(port)
    app.config.defaultContentType = "application/json"
    app.post("/request") { ctx ->
        ctx.status(200)
        toJsonOrThrow(ctx) { FlatService.requestViewing(ctx.body<RequestCancelViewingDto>()) }
    }
    app.patch("/cancel") { ctx ->
        ctx.status(204)
        toJsonOrThrow(ctx) { FlatService.cancelViewing(ctx.body<RequestCancelViewingDto>()) }
    }
    app.put("/approve") { ctx ->
        ctx.status(200)
        toJsonOrThrow(ctx) { FlatService.approveViewing(ctx.body<ApproveRejectViewingDto>()) }
    }
    app.put("/reject") { ctx ->
        ctx.status(200)
        toJsonOrThrow(ctx) { FlatService.rejectViewing(ctx.body<ApproveRejectViewingDto>()) }
    }
    return app
}

private fun toJsonOrThrow(ctx: Context, function: () -> Any) =
    runCatching { function() }
        .fold(
            {
                ctx.json(it)
            }, {
                when (it) {
                    is Exception -> throw BadRequestResponse(it.localizedMessage ?: "Unknown errrorrr")
                    else -> throw InternalServerErrorResponse(it.localizedMessage ?: "Unknown errrorrr")
                }
            }
        )
