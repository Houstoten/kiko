package com.kiko.flat

import com.kiko.flat.dto.ApproveRejectViewingDto
import com.kiko.flat.dto.RequestCancelViewingDto
import com.kiko.flat.exceptions.CannotReserveOnThisDate
import com.kiko.flat.exceptions.NoSuchReservationException
import com.kiko.flat.exceptions.WrongDateException
import com.kiko.notification.NotificationService
import com.kiko.notification.dto.NotifyTenantDto
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

    fun requestViewing(dtoCancel: RequestCancelViewingDto): ClosedRange<LocalDateTime> {
        if (checkTimeCommon(dtoCancel.date)) {

            val range = dtoCancel.date..dtoCancel.date.plusMinutes(viewingSlotRange)
            val currentTenantId = FlatRepository.requestViewing(dtoCancel.flatId, dtoCancel.tenantId, range)
                ?: throw CannotReserveOnThisDate(dtoCancel.flatId.toString(), range.start)

            NotificationService.notifyCurrentTenantOfNewRequest(
                NotifyTenantDto(dtoCancel.flatId, range, dtoCancel.tenantId, currentTenantId)
            )

            return range
        } else {
            throw CannotReserveOnThisDate(dtoCancel.flatId.toString(), dtoCancel.date)
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

            val pair = FlatRepository.approveViewing(dto.flatId, dto.currentTenantId, range)
                ?: throw NoSuchReservationException(dto.flatId.toString(), range.start)

            NotificationService.notifyNewTenantOfReservationApprovement(
                NotifyTenantDto(dto.flatId, range, pair.first, dto.currentTenantId)
            )

        } else {
            throw WrongDateException(dto.date)
        }
    }

    fun rejectViewing(dto: ApproveRejectViewingDto) {
        if (checkTimeCommon(dto.date)) {
            val range = dto.date..dto.date.plusMinutes(viewingSlotRange)

            val pair = FlatRepository.rejectViewing(dto.flatId, dto.currentTenantId, range)
                ?: throw NoSuchReservationException(dto.flatId.toString(), range.start)

            NotificationService.notifyNewTenantOfReservationRejection(
                NotifyTenantDto(dto.flatId, range, pair.first, dto.currentTenantId)
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
    }// TODO: 01.10.2020 timer
}