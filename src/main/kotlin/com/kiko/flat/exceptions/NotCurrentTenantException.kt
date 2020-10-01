package com.kiko.flat.exceptions

class NotCurrentTenantException(flatId: String): RuntimeException("You are not renting flat with id $flatId")