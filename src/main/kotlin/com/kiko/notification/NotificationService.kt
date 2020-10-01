package com.kiko.notification

import com.kiko.notification.dto.NotifyCurrentDto

object NotificationService {
    fun notifyCurrentTenantOfNewRequest(dto: NotifyCurrentDto) =
            println("*New request for ${dto.currentTenantId}* ${dto.tenantId} wants to visit ${dto.flatId} at ${dto.viewingTime}")
}