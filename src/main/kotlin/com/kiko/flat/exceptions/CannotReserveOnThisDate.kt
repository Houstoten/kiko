package com.kiko.flat.exceptions

import java.time.LocalDateTime

class CannotReserveOnThisDate(flatId: String, date: LocalDateTime) :
        RuntimeException("Cannot book visit to flat $flatId on $date")