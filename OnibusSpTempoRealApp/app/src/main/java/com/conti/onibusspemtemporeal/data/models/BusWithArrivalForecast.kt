package com.conti.onibusspemtemporeal.data.models

data class BusWithArrivalForecast(
    val fullPlacard: String,
    val busDestiny: String,
    val busArrivalForecast: String,
    val busPrefix: Int,
    val busAccessible: Boolean
)