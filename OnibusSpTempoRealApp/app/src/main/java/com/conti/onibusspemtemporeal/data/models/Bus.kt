package com.conti.onibusspemtemporeal.data.models


import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.time.LocalTime
import java.time.ZoneId
import java.util.*


data class Bus(
    @SerializedName("p")
    val prefixBus: Int,
    @SerializedName("t")
    val busArrivalForecast: String?,
    @SerializedName("a")
    val acessibleBus: Boolean,
    @SerializedName("ta")
    val utcGetStatusHour: String,
    @SerializedName("py")
    val latBus: Double,
    @SerializedName("px")
    val longBus: Double
) : Serializable
