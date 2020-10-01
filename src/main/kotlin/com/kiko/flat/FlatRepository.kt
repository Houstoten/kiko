package com.kiko.flat

import com.kiko.flat.exceptions.AlreadyTenantException
import com.kiko.flat.exceptions.CannotReserveOnThisDate
import com.kiko.flat.exceptions.NoSuchFlatException
import com.kiko.flat.model.Flat
import java.lang.RuntimeException
import java.time.LocalDateTime

import java.util.concurrent.ConcurrentHashMap

object FlatRepository {
    const val viewingSlotRange: Long = 1200 //in seconds
    val daySlots: ClosedRange<Int> = IntRange(10, 20) //daily hours range
    val flats: Map<Int, Flat> = ConcurrentHashMap(mutableMapOf(
            1 to Flat(1, 1, HashMap(), ConcurrentHashMap()),
            2 to Flat(2, 2, HashMap(), ConcurrentHashMap()),
            3 to Flat(3, 3, HashMap(), ConcurrentHashMap())
    ))

    @Synchronized
    fun bookSomeDate(flatId: Int, tenantId: Int, range: ClosedRange<LocalDateTime>): Int? =
            ((flats[flatId] ?: throw NoSuchFlatException(flatId.toString()))
                    .takeIf {
                        tenantId != it.currentTenantId
                    } ?: throw AlreadyTenantException(flatId.toString()))
                    .reservations.putIfAbsent(range, tenantId)

//                    .takeIf {
//                        it == tenantId
//                    }
//                    ?: throw CannotReserveOnThisDate(flatId.toString(), range.start)



}