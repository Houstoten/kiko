package com.kiko.flat.model

import java.time.LocalDateTime

class Flat(
    val currentTenantId: Int?,
    var currentWeek: Map<ClosedRange<LocalDateTime>, Pair<Int, Boolean?>>,
    var reservations: MutableMap<ClosedRange<LocalDateTime>, Pair<Int, Boolean?>>
    //Map<timeRange, Pair<newTenantId, approved>>
) //if approve is null so request still pending
