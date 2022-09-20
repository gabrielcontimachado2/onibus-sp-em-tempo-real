package com.conti.onibusspemtemporeal.util

import androidx.recyclerview.widget.DiffUtil

import com.conti.onibusspemtemporeal.data.models.BusWithArrivalForecast

class DiffUtilBusArrivalForecast : DiffUtil.ItemCallback<BusWithArrivalForecast>() {

    override fun areItemsTheSame(
        oldItem: BusWithArrivalForecast,
        newItem: BusWithArrivalForecast
    ): Boolean {
        return oldItem.busDestiny == newItem.busDestiny
    }

    override fun areContentsTheSame(
        oldItem: BusWithArrivalForecast,
        newItem: BusWithArrivalForecast
    ): Boolean {
        return oldItem == newItem
    }
}