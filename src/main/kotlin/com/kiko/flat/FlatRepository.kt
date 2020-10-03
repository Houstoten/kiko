package com.kiko.flat

import com.kiko.Reservation
import com.kiko.flat.exceptions.AlreadyTenantException
import com.kiko.flat.exceptions.BadCredentialsException
import com.kiko.flat.exceptions.NoSuchFlatException
import com.kiko.flat.exceptions.NotCurrentTenantException
import com.kiko.flat.model.Flat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*

import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer

object FlatRepository {

    private val reservations: MutableMap<Pair<Int, ClosedRange<LocalDateTime>>, Reservation> =
        ConcurrentHashMap(mutableMapOf())

    private val flats: MutableMap<Int, Flat> = ConcurrentHashMap(
        mutableMapOf(
            1 to Flat(1, 1),
            2 to Flat(3, 2),
            3 to Flat(3, 3)
        )
    )

    private fun checkForFlatAndTenant(flatId: Int, tenantId: Int): Flat =
        checkForTenant(checkForFlat(flatId), tenantId, flatId)

    private fun checkForFlat(flatId: Int): Flat = (flats[flatId] ?: throw NoSuchFlatException(flatId.toString()))

    private fun checkForNewTenant(flat: Flat, tenantId: Int, range: ClosedRange<LocalDateTime>): Flat =
        flat.takeIf {
            tenantId == reservations[Pair(it.id, range)]?.tenantId
        } ?: throw BadCredentialsException()

    private fun checkForTenant(flat: Flat, tenantId: Int, flatId: Int): Flat =
        flat.takeIf {
            tenantId != it.currentTenantId
        } ?: throw AlreadyTenantException(flatId.toString())

    private fun checkForCurrentTenant(flat: Flat, tenantId: Int, flatId: Int): Flat =
        flat.takeIf {
            tenantId == it.currentTenantId
        } ?: throw NotCurrentTenantException(flatId.toString())

    @Synchronized
    fun requestViewing(flatId: Int, tenantId: Int, range: ClosedRange<LocalDateTime>): Int? =
        checkForFlatAndTenant(flatId, tenantId)
            .takeIf {
                reservations.putIfAbsent(Pair(it.id, range), Reservation(range, it.id, tenantId)) == null
            }?.currentTenantId

    @Synchronized
    fun cancelViewing(flatId: Int, tenantId: Int, range: ClosedRange<LocalDateTime>): Int? =
        checkForNewTenant(checkForFlat(flatId), tenantId, range)
            .takeIf {
                reservations.containsKey(Pair(it.id, range)) && reservations.remove(Pair(flatId, range)) != null
            }?.currentTenantId

    @Synchronized
    fun approveViewing(flatId: Int, currentTenantId: Int, range: ClosedRange<LocalDateTime>): Int? =
        checkForCurrentTenant(checkForFlat(flatId), currentTenantId, flatId)
            .let {
                reservations[Pair(flatId, range)]
                    ?.takeIf { it.approved == null }
                    ?.let { it.approved = true; it.tenantId }
            }

    @Synchronized
    fun rejectViewing(flatId: Int, currentTenantId: Int, range: ClosedRange<LocalDateTime>): Int? =
        checkForCurrentTenant(checkForFlat(flatId), currentTenantId, flatId)
            .let {
                reservations[Pair(flatId, range)]
                    ?.takeIf { it.approved == null }
                    ?.let { it.approved = false; it.tenantId }
            }
}