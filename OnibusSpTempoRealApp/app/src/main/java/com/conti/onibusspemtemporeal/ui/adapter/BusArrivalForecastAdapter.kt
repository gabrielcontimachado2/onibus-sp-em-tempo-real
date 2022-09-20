package com.conti.onibusspemtemporeal.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.conti.onibusspemtemporeal.data.models.BusRoute
import com.conti.onibusspemtemporeal.data.models.BusWithArrivalForecast
import com.conti.onibusspemtemporeal.databinding.CardBusArrivalForecastBinding
import com.conti.onibusspemtemporeal.util.DiffUtilBusArrivalForecast

class BusArrivalForecastAdapter :
    ListAdapter<BusWithArrivalForecast, BusArrivalForecastAdapter.BusArrivalForecastViewHolder>(
        DiffUtilBusArrivalForecast()
    ) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BusArrivalForecastViewHolder {
        val binding =
            CardBusArrivalForecastBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return BusArrivalForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusArrivalForecastViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    private var onCardClickListener: ((BusRoute) -> Unit)? = null

    fun setonCardClickListener(listener: (BusRoute) -> Unit) {
        onCardClickListener = listener
    }

    private var onImageFavoriteClickListener: ((BusRoute) -> Unit)? = null

    fun setonImageFavoriteClickListener(listener: (BusRoute) -> Unit) {
        onImageFavoriteClickListener = listener
    }

    inner class BusArrivalForecastViewHolder(private val binding: CardBusArrivalForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(busArrivalForecast: BusWithArrivalForecast) {
            binding.apply {

                textViewPlacardFull.text = busArrivalForecast.fullPlacard
                textViewDestinyFromApi.text = busArrivalForecast.busDestiny
                textViewBusArrivalForecastFromApi.text = busArrivalForecast.busArrivalForecast
                textViewBusPrefixFromApi.text = busArrivalForecast.busPrefix.toString()
                textViewAccessibleBusFromApi.text =
                    if (busArrivalForecast.busAccessible) "Sim" else "Não"

            }
        }

    }
}