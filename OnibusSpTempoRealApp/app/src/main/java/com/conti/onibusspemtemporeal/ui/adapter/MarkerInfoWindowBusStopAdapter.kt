package com.conti.onibusspemtemporeal.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.conti.onibusspemtemporeal.R
import com.conti.onibusspemtemporeal.data.models.BusStop
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowBusStopAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker): View? {

        val busStop = p0.tag as? BusStop ?: return null

        val view = LayoutInflater.from(context).inflate(
            R.layout.marker_info_bus_stop_contents, null
        )

        view.findViewById<TextView>(
            R.id.textView_stop_name_from_api
        ).text = busStop.stopName.lowercase().replaceFirstChar { it.uppercase() }
        view.findViewById<TextView>(
            R.id.textView_address_bus_stop_from_api
        ).text = busStop.stopAddress.lowercase().replaceFirstChar { it.uppercase() }

        return view
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }

}
