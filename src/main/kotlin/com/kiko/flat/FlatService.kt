package com.kiko.flat

import com.kiko.flat.dto.ApproveRejectViewingDto
import com.kiko.flat.dto.RequestCancelViewingDto
import com.kiko.flat.dto.SuccessfulRequestDto
import com.kiko.flat.exceptions.CannotReserveOnThisDate
import com.kiko.flat.exceptions.NoSuchReservationException
import com.kiko.flat.exceptions.WrongDateException
import com.kiko.notification.NotificationService
import com.kiko.notification.dto.NotifyTenantDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.fixedRateTimer

object FlatService {
    private const val viewingSlotRange: Long = 20 //in minutes
    private val daySlots: ClosedRange<LocalTime> =
        LocalTime.of(10, 0)..LocalTime.of(19, 40)  //daily minutes range
    private val days: ClosedRange<LocalDate> = newWeekSwapper()

    fun requestViewing(dto: RequestCancelViewingDto): SuccessfulRequestDto {
        if (checkTimeCommon(dto.date)) {

            val range = dto.date..dto.date.plusMinutes(viewingSlotRange)
            val currentTenantId = FlatRepository.requestViewing(dto.flatId, dto.tenantId, range)
                ?: throw CannotReserveOnThisDate(dto.flatId.toString(), range.start)

            NotificationService.notifyCurrentTenantOfNewRequest(
                NotifyTenantDto(dto.flatId, range, dto.tenantId, currentTenantId)
            )

            return SuccessfulRequestDto(dto.flatId, range.start, range.endInclusive)
        } else {
            throw CannotReserveOnThisDate(dto.flatId.toString(), dto.date)
        }
    }

    fun cancelViewing(dtoCancel: RequestCancelViewingDto) {
        if (checkTimeCommon(dtoCancel.date)) {
            val range = dtoCancel.date..dtoCancel.date.plusMinutes(viewingSlotRange)

            val currentTenantId = FlatRepository.cancelViewing(dtoCancel.flatId, dtoCancel.tenantId, range)
                ?: throw NoSuchReservationException(dtoCancel.flatId.toString(), range.start)

            NotificationService.notifyCurrentTenantOfReservationCancellation(
                NotifyTenantDto(dtoCancel.flatId, range, dtoCancel.tenantId, currentTenantId)
            )
        } else {
            throw WrongDateException(dtoCancel.date)
        }
    }

    fun approveViewing(dto: ApproveRejectViewingDto) {
        if (checkTimeCommon(dto.date)) {
            val range = dto.date..dto.date.plusMinutes(viewingSlotRange)

            val tenantId = FlatRepository.approveViewing(dto.flatId, dto.currentTenantId, range)
                ?: throw NoSuchReservationException(dto.flatId.toString(), range.start)

            NotificationService.notifyNewTenantOfReservationApprovement(
                NotifyTenantDto(dto.flatId, range, tenantId, dto.currentTenantId)
            )

        } else {
            throw WrongDateException(dto.date)
        }
    }

    fun rejectViewing(dto: ApproveRejectViewingDto) {
        if (checkTimeCommon(dto.date)) {
            val range = dto.date..dto.date.plusMinutes(viewingSlotRange)

            val tenantId = FlatRepository.rejectViewing(dto.flatId, dto.currentTenantId, range)
                ?: throw NoSuchReservationException(dto.flatId.toString(), range.start)

            NotificationService.notifyNewTenantOfReservationRejection(
                NotifyTenantDto(dto.flatId, range, tenantId, dto.currentTenantId)
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

    private fun newWeekSwapper(): ClosedRange<LocalDate> {
        fixedRateTimer(
            "weekSwapper",
            true,
            ChronoUnit.MILLIS.between(
                LocalDateTime.now(),
                LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)).atStartOfDay()
            ),
            1000L * 60L * 60L * 24L * 7L
        ) { createSlotsForUpcomingWeek() }

        return createSlotsForUpcomingWeek()
    }
}