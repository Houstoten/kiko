package com.kiko

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kiko.flat.FlatService
import com.kiko.flat.dto.RequestViewingDto
import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import io.javalin.http.InternalServerErrorResponse
import java.lang.Exception

fun main() {
    val config = Configuration.readConfiguration();
    val app = Javalin.create().start(config[ApplicationConfiguration.port])
    app.post("/request") { ctx ->
        ctx.contentType("application/json")
        val mapper = jacksonObjectMapper()

        val body = ctx.body<RequestViewingDto>()
        runCatching { FlatService.requestViewing(body) }
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
    }
}
