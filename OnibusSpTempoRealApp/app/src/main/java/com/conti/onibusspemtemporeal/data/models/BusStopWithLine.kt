package com.conti.onibusspemtemporeal.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BusStopWithLine(
    @SerializedName("cp")
    val stopCod: Int,
    @SerializedName("np")
    val stopName: String,
    @SerializedName("py")
    val stopLat: Double,
    @SerializedName("px")
    val stopLng: Double,
    @SerializedName("l")
    val busRouteWithBus: List<BusRouteWithBus>
):Serializable