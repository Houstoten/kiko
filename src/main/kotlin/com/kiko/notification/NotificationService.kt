package com.kiko.notification

import com.kiko.notification.dto.NotifyTenantDto

object NotificationService {
    fun notifyCurrentTenantOfNewRequest(dto: NotifyTenantDto) =
            println("*New request for ${dto.currentTenantId}* ${dto.tenantId} wants to visit ${dto.flatId} at ${dto.viewingTime}")

    fun notifyCurrentTenantOfReservationCancellation(dto: NotifyTenantDto) =
        println("*Notification for ${dto.currentTenantId}* ${dto.tenantId} cancelled visit to ${dto.flatId} at ${dto.viewingTime}")

    fun notifyNewTenantOfReservationApprovement(dto: NotifyTenantDto) =
        println("*Notification for ${dto.tenantId}* ${dto.currentTenantId} approved your visit to ${dto.flatId} at ${dto.viewingTime}")

    fun notifyNewTenantOfReservationRejection(dto: NotifyTenantDto) =
        println("*Notification for ${dto.tenantId}* ${dto.currentTenantId} rejected your visit to ${dto.flatId} at ${dto.viewingTime}")
}