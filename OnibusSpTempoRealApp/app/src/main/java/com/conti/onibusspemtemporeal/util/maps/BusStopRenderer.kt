package com.conti.onibusspemtemporeal.util.maps

import android.content.Context
import androidx.core.content.ContextCompat
import com.conti.onibusspemtemporeal.R
import com.conti.onibusspemtemporeal.data.models.BusStop
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class BusStopRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<BusStop>
) : DefaultClusterRenderer<BusStop>(context, map, clusterManager) {

    private val iconBusStopBitmap: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(
            context,
            R.color.black
        )
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.ic_bus_stop_big,
            color
        )
    }


    override fun onBeforeClusterItemRendered(
        item: BusStop,
        markerOptions: MarkerOptions
    ) {
        markerOptions
            .position(LatLng(item.stopLat, item.stopLng))
            .icon(iconBusStopBitmap)
    }


    override fun onClusterItemRendered(clusterItem: BusStop, marker: Marker) {
        marker.tag = clusterItem
    }
}