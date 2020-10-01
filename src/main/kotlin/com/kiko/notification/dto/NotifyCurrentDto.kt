package com.kiko.notification.dto

import java.time.LocalDateTime

data class NotifyCurrentDto(
        val flatId: Int,
        val viewingTime: ClosedRange<LocalDateTime>,
        val tenantId: Int,
        val currentTenantId: Int
)