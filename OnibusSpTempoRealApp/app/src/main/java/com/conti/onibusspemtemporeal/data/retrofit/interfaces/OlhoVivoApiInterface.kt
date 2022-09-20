package com.conti.onibusspemtemporeal.data.retrofit.interfaces

import com.conti.onibusspemtemporeal.data.models.BusRoute
import com.conti.onibusspemtemporeal.data.models.BusStop
import com.conti.onibusspemtemporeal.data.models.ResponseAllBus
import com.conti.onibusspemtemporeal.data.models.ResponseBusArrivalForecast
import retrofit2.Response

interface OlhoVivoApiInterface {

    suspend fun getRoutes(searchTerm: String): Response<List<BusRoute>>

    suspend fun getAllBus(): Response<ResponseAllBus>

    suspend fun getBusStopByLineCode(lineCod: Int): Response<List<BusStop>>

    suspend fun getBusArrivalForecastByBusStop(busStopCod: Int): Response<ResponseBusArrivalForecast>
}
