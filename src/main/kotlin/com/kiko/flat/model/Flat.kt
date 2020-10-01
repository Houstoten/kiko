package com.kiko.flat.model

import java.time.LocalDateTime

class Flat(val flatId: Int,
           val currentTenantId: Int?,
           val currentWeek: Map<ClosedRange<LocalDateTime>, Int?>, //Map<timeRange, newTenantId>
           val reservations: MutableMap<ClosedRange<LocalDateTime>, Int?>){//if tenantId null so reservation banned

    override fun hashCode(): Int = flatId

    override fun equals(other: Any?): Boolean = (other is Flat) && other.flatId == flatId

}