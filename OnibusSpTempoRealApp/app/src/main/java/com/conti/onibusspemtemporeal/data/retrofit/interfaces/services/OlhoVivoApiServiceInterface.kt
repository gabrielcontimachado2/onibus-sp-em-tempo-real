package com.conti.onibusspemtemporeal.data.retrofit.interfaces.services

import com.conti.onibusspemtemporeal.data.models.BusRoute
import com.conti.onibusspemtemporeal.data.models.BusStop
import com.conti.onibusspemtemporeal.data.models.ResponseAllBus
import com.conti.onibusspemtemporeal.data.models.ResponseBusArrivalForecast
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OlhoVivoApiServiceInterface {

    @GET("Linha/Buscar")
    suspend fun getRoutes(
        @Query("termosBusca")
        searchTerm: String
    ): Response<List<BusRoute>>

    @GET("Posicao")
    suspend fun getAllBus(): Response<ResponseAllBus>

    @GET("Parada/BuscarParadasPorLinha")
    suspend fun getBusStopByLineCode(
        @Query("codigoLinha")
        lineCod: Int
    ): Response<List<BusStop>>

    @GET("Parada/Buscar")
    suspend fun getAllBusStop(
        @Query("termosBusca")
        termoBusca: String = ""
    ): Response<List<BusStop>>

    @GET("Previsao/Parada")
    suspend fun getBusArrivalForecastByBusStop(
        @Query("codigoParada")
        busStopCod: Int
    ): Response<ResponseBusArrivalForecast>


}