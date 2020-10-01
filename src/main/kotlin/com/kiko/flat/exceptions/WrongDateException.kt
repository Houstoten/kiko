package com.kiko.flat.exceptions

import java.time.LocalDateTime

class WrongDateException (date: LocalDateTime) :
    RuntimeException("Check date you pass, please: $date")