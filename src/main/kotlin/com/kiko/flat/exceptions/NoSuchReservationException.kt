package com.kiko.flat.exceptions

import java.time.LocalDateTime

class NoSuchReservationException(flatId: String, date: LocalDateTime) :
    RuntimeException("Sorry, there is no reservation on: $date for flat $flatId")