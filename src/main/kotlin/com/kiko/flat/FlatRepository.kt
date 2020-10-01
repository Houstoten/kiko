package com.kiko.flat

import com.kiko.flat.exceptions.AlreadyTenantException
import com.kiko.flat.exceptions.NoSuchFlatException
import com.kiko.flat.exceptions.NotCurrentTenantException
import com.kiko.flat.model.Flat
import java.time.LocalDateTime

import java.util.concurrent.ConcurrentHashMap

object FlatRepository {

    private val flats: Map<Int, Flat> = ConcurrentHashMap(mutableMapOf(
            1 to Flat(1, 1, HashMap(), ConcurrentHashMap()),
            2 to Flat(2, 2, HashMap(), ConcurrentHashMap()),
            3 to Flat(3, 3, HashMap(), ConcurrentHashMap())
    ))

    private fun checkForFlatAndTenant(flatId: Int, tenantId: Int): Flat =
            checkForTenant(checkForFlat(flatId), tenantId)

    private fun checkForFlat(flatId: Int): Flat = (flats[flatId] ?: throw NoSuchFlatException(flatId.toString()))

    private fun checkForTenant(flat: Flat, tenantId: Int): Flat =
            flat.takeIf {
                tenantId != it.currentTenantId
            } ?: throw AlreadyTenantException(flat.flatId.toString())

    private fun checkForCurrentTenant(flat: Flat, tenantId: Int): Flat =
            flat.takeIf {
                tenantId == it.currentTenantId
            } ?: throw NotCurrentTenantException(flat.flatId.toString())

    @Synchronized
    fun requestViewing(flatId: Int, tenantId: Int, range: ClosedRange<LocalDateTime>): Int? =
            checkForFlatAndTenant(flatId, tenantId)
                    .takeIf {
                        it.reservations.putIfAbsent(range, Pair(tenantId, false)) == null
                    }?.currentTenantId

    @Synchronized
    fun cancelViewing(flatId: Int, tenantId: Int, range: ClosedRange<LocalDateTime>): Pair<Int, Boolean>? =
            checkForFlatAndTenant(flatId, tenantId)
                    .reservations.remove(range)

    @Synchronized
    fun approveViewing(flatId: Int, currentTenantId: Int, range: ClosedRange<LocalDateTime>): Pair<Int, Boolean>? =
            checkForCurrentTenant(checkForFlat(flatId), currentTenantId)
                    .reservations.computeIfPresent(range) { _, v -> v.copy(second = true) }

    @Synchronized
    fun rejectViewing(flatId: Int, currentTenantId: Int, range: ClosedRange<LocalDateTime>): Pair<Int, Boolean>? =
            checkForCurrentTenant(checkForFlat(flatId), currentTenantId)
                    .reservations.computeIfPresent(range) { _, _ -> null }


}