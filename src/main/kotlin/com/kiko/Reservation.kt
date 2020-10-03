package com.kiko

import java.time.LocalDateTime

data class Reservation(
    val range: ClosedRange<LocalDateTime>,
    val flatId: Int,
    val tenantId: Int,
    var approved: Boolean? = null
)