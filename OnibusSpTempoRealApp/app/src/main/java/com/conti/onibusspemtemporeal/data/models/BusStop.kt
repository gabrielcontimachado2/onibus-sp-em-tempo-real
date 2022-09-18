package com.conti.onibusspemtemporeal.data.models

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem
import java.io.Serializable

class BusStop(
    @SerializedName("cp")
    val stopCod: Int,
    @SerializedName("np")
    val stopName: String,
    @SerializedName("ed")
    val stopAddress: String,
    @SerializedName("py")
    val stopLat: Double,
    @SerializedName("px")
    val stopLng: Double

) : Serializable, ClusterItem {

    override fun getPosition(): LatLng {
        return LatLng(stopLat, stopLng)
    }

    override fun getTitle(): String? {
        return stopName
    }

    override fun getSnippet(): String? {
        return stopAddress
    }
}
