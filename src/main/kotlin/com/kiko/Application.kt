package com.kiko

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.kiko.flat.FlatService
import com.kiko.flat.dto.RequestViewingDto
import io.javalin.Javalin

fun main() {
    val config = Configuration.readConfiguration();
    val app = Javalin.create().start(config[ApplicationConfiguration.port])
    val gson = Gson()
    app.post("/request") { ctx ->
        ctx.contentType("application/json")
        val mapper = jacksonObjectMapper()

        val body = ctx.body<RequestViewingDto>()
//        mapper.readValue<RequestViewingDto>(ctx.body())
        FlatService.requestViewing(body)?.let { ctx.json(it) }
//        println(mapper ctx.body())
    }
}
