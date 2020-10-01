package com.kiko.flat.exceptions

class NoSuchFlatException(flatId: String) : RuntimeException("Cannot find flat with id $flatId")