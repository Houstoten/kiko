package com.kiko.flat.exceptions

class AlreadyTenantException(flatId: String): RuntimeException("You already rent flat with id $flatId")