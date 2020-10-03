package com.kiko.reservation

import java.time.LocalDateTime

data class Reservation(
    val range: ClosedRange<LocalDateTime>,
    val flatId: Int,
    val tenantId: Int,
    var approved: Boolean? = null
)