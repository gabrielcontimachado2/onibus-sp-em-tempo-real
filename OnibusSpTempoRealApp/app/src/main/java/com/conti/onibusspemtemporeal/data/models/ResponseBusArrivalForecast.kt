package com.conti.onibusspemtemporeal.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResponseBusArrivalForecast(
    @SerializedName("hr")
    val hourRefer: String,
    @SerializedName("p")
    val busStopWithLineAndBus: BusStopWithLine
) : Serializable