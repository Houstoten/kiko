package com.kiko.flat

import com.kiko.flat.dto.RequestViewingDto
import com.kiko.flat.exceptions.CannotReserveOnThisDate
import com.kiko.flat.exceptions.NoSuchReservationException
import com.kiko.flat.exceptions.WrongDateException
import com.kiko.notification.NotificationService
import com.kiko.notification.dto.NotifyCurrentDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

object FlatService {
    private const val viewingSlotRange: Long = 20 //in minutes
    private val daySlots: ClosedRange<LocalTime> =
        LocalTime.of(10, 0)..LocalTime.of(19, 40)  //daily minutes range
    private val days: ClosedRange<LocalDate> = createSlotsForUpcomingWeek()

    fun requestViewing(dto: RequestViewingDto): ClosedRange<LocalDateTime> {
        if (checkTimeCommon(dto.date)) {

            val range = dto.date..dto.date.plusMinutes(viewingSlotRange)
            val currentTenantId = FlatRepository.requestViewing(dto.flatId, dto.tenantId, range)
                ?: throw CannotReserveOnThisDate(dto.flatId.toString(), range.start)

            NotificationService.notifyCurrentTenantOfNewRequest(
                NotifyCurrentDto(dto.flatId, range, dto.tenantId, currentTenantId)
            )

            return range
        } else {
            throw CannotReserveOnThisDate(dto.flatId.toString(), dto.date)
        }
    }

    fun cancelViewing(dto: RequestViewingDto) {
        if (checkTimeCommon(dto.date)) {
            val range = dto.date..dto.date.plusMinutes(viewingSlotRange)

            val currentTenantId = FlatRepository.cancelViewing(dto.flatId, dto.tenantId, range)
                ?: throw NoSuchReservationException(dto.flatId.toString(), range.start)

            NotificationService.notifyCurrentTenantOfReservationCancellation(
                NotifyCurrentDto(dto.flatId, range, dto.tenantId, currentTenantId)
            )
        } else {
            throw WrongDateException(dto.date)
        }
    }

    private fun checkTimeCommon(date: LocalDateTime): Boolean =
        days.contains(date.toLocalDate())
                && daySlots.contains(date.toLocalTime())
                && date.minusHours(24).isAfter(LocalDateTime.now())
                && date.minute % 20 == 0

    private fun createSlotsForUpcomingWeek(): ClosedRange<LocalDate> {
        val monday: LocalDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        val sunday: LocalDate = monday.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
        return monday..sunday
    }
}