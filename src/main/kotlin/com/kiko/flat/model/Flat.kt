package com.kiko.flat.model

import java.time.LocalDateTime

class Flat(val flatId: Int,
           val currentTenantId: Int?,
           val currentWeek: Map<ClosedRange<LocalDateTime>, Pair<Int, Boolean?>>, //Map<timeRange, Pair<newTenantId, approved>>
           val reservations: MutableMap<ClosedRange<LocalDateTime>, Pair<Int, Boolean?>>){//if pair is null so date is banned

    override fun hashCode(): Int = flatId

    override fun equals(other: Any?): Boolean = (other is Flat) && other.flatId == flatId

}