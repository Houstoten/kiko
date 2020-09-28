package com.kiko

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.yaml

object ApplicationConfiguration : ConfigSpec("server") {
    val port by required<Int>()
}

object Configuration {
    fun readConfiguration(): Config {
        val content = this::class.java.classLoader.getResource("config.yml").openStream()
        return  Config{ addSpec(ApplicationConfiguration) }.from.yaml.inputStream(content)
    }
}