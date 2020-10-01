package com.kiko.flat

import com.kiko.flat.dto.RequestViewingDto
import com.kiko.flat.exceptions.CannotReserveOnThisDate
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

    fun requestViewing(dto: RequestViewingDto): ClosedRange<LocalDateTime>? {
        if (days.contains(dto.date.toLocalDate())
                && daySlots.contains(dto.date.toLocalTime())
                && dto.date.minusHours(24).isAfter(LocalDateTime.now())
                && dto.date.minute % 20 == 0) {
            val range = dto.date..dto.date.plusMinutes(viewingSlotRange)
            val currentTenantId = FlatRepository.requestViewing(dto.flatId, dto.tenantId, range)
                    ?: throw CannotReserveOnThisDate(dto.flatId.toString(), range.start)

            NotificationService.notifyCurrentTenantOfNewRequest(
                    NotifyCurrentDto(dto.flatId, range, dto.tenantId, currentTenantId))

            return range
        }
        return null
    }

    private fun createSlotsForUpcomingWeek(): ClosedRange<LocalDate> {
        val monday: LocalDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        val sunday: LocalDate = monday.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
        return monday..sunday
    }
}