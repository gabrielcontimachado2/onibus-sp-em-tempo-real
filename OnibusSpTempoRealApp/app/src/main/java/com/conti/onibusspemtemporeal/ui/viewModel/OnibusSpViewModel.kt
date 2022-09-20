package com.conti.onibusspemtemporeal.ui.viewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conti.onibusspemtemporeal.data.models.*
import com.conti.onibusspemtemporeal.domain.apiRepository.OlhoVivoApiRepository
import com.conti.onibusspemtemporeal.domain.roomRepository.RoomRepository
import com.conti.onibusspemtemporeal.util.Constants.START_CURRENT_LINE_CODE
import com.conti.onibusspemtemporeal.util.Constants.START_CURRENT_LOCATION_USER
import com.conti.onibusspemtemporeal.util.Constants.START_HOUR_UPDATE
import com.conti.onibusspemtemporeal.util.Constants.START_LINE_WAY
import com.conti.onibusspemtemporeal.util.Constants.START_MESSAGE
import com.conti.onibusspemtemporeal.util.Constants.START_QUANTITY_BUS
import com.conti.onibusspemtemporeal.util.Constants.START_QUANTITY_BUS_STOP
import com.conti.onibusspemtemporeal.util.retrofitHandling.Resource
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class OnibusSpViewModel @Inject constructor(
    private val apiRepository: OlhoVivoApiRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {


    private val _authenticate = MutableLiveData<Boolean>(false)

    private val _busRoute = MutableLiveData<Resource<List<BusRoute>>>()
    val busRoute: LiveData<Resource<List<BusRoute>>>
        get() = _busRoute

    //UI STATES
    private val _uiStateMainActivity: MutableStateFlow<UiStateMainActivity> =
        MutableStateFlow(UiStateMainActivity())
    val uiStateMainActivity: StateFlow<UiStateMainActivity>
        get() = _uiStateMainActivity.asStateFlow()

    private val _uiStateMapFragment: MutableStateFlow<UiStateMapFragment> =
        MutableStateFlow(UiStateMapFragment())
    val uiStateMapFragment: StateFlow<UiStateMapFragment>
        get() = _uiStateMapFragment.asStateFlow()

    private val _uiStateBusStopDialogFragment: MutableStateFlow<UiStateBusStopDialogFragment> =
        MutableStateFlow(UiStateBusStopDialogFragment())
    val uiStateBusStopDialogFragment: StateFlow<UiStateBusStopDialogFragment>
        get() = _uiStateBusStopDialogFragment.asStateFlow()


    init {

        //Por falta de tempo ainda não coloquei um refresh na autenticação por uma quantidade de tempo
        authenticate()

        viewModelScope.launch {
            getFavoritesBusRoute()
        }

    }


    /** Função para realizar requisição da posição de todos os ônibus, coloco [_uiStateMapFragment] como loading true, e realizo dentro do try e catch,
     * a requisição utilizando o repositorio da api, utilizo da classe [handleBusRoutersResponse] para manipular a resposta e atualizar ela para
     * a [_uiStateMapFragment], caso tenha algum erro no processo, atualizo a [_uiStateMapFragment] com a mensagem do erro */
    fun getBus() {
        viewModelScope.launch {

            _uiStateMapFragment.update {
                it.copy(isLoading = true)
            }

            try {
                if (_authenticate.value!!) {
                    val response = apiRepository.getAllBus()

                    handleBusResponse(response)
                } else {

                    _uiStateMainActivity.update {
                        it.copy(message = "Erro no sistema, tente novamente")
                    }
                    authenticate()
                }
            } catch (t: Throwable) {
                _uiStateMainActivity.update {
                    it.copy(message = t.message.toString())
                }
            }

        }
    }

    /** Função para manipular uma resposta de linha em relação aos ônibus, caso a resposta for de sucesso
     * crio uma variável para armazenar a linha e os ônibus em circulação delas, após armazernas todos os õnibus
     * atualizo o [_uiStateMapFragment] com essa lista de onibus, e para finalizar a função retorno sucesso, caso não tenha entrado no if
     * finalizo a função retornando erro */
    private fun handleBusResponse(response: Response<ResponseAllBus>) {

        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                val currentListBusWithRoute: MutableList<BusWithLine> = mutableListOf()
                var quantityBus = 0

                resultResponse.lineRelation.forEach { busRouteWithBus ->

                    quantityBus += busRouteWithBus.amountBusFound

                    busRouteWithBus.buses.forEach { bus ->

                        val latLng = LatLng(bus.latBus, bus.longBus)

                        val busWithLine = BusWithLine(
                            busRouteWithBus.fullPlacard,
                            bus.prefixBus,
                            busRouteWithBus.originPlacard,
                            busRouteWithBus.destinyPlacard,
                            bus.acessibleBus,
                            resultResponse.hourGet,
                            latLng,
                            busRouteWithBus.lineCod
                        )

                        currentListBusWithRoute.add(busWithLine)
                    }
                }

                _uiStateMapFragment.update {
                    it.copy(
                        currentBuses = currentListBusWithRoute,
                        isLoading = false,
                    )
                }

                _uiStateMainActivity.update {
                    it.copy(currentQuantityBus = quantityBus)
                }

            }
        } else {
            _uiStateMainActivity.update {
                it.copy(message = "Não foi possivel carregar ônibus, tente novamente")
            }
        }
    }


    /** Função para realizar uma requisição de GET na [apiRepository], consultar as linhas de onibus
     * disponiveis de acordo com o que foi digitado pelo usuário e armazenado na, [busRoute],
     * ao começar a função [_busRoute] recebe estado de Loading, e em um try e catch realizo a requisição e atualizo [_busRoute] com o resultado da
     * lista de linhas disponiveis ou caso não tenha disponivel ou ocorra algum problema na requisição,
     * o cath retorna a mensagem para atualizar [uiStateMapFragment]*/
    fun searchBusRoute(busRoute: String) {
        viewModelScope.launch {
            _busRoute.postValue(Resource.Loading())
            try {
                if (_authenticate.value!!) {
                    val response = apiRepository.getRoutes(busRoute)
                    _busRoute.postValue(handleBusRoutersResponse(response))
                } else {
                    _uiStateMainActivity.update {
                        it.copy(message = "Verfique sua conexão, o aplicativo não funciona sem internet ")
                    }
                }
            } catch (t: Throwable) {
                _uiStateMainActivity.update {
                    it.copy(message = t.message.toString())
                }
            }
        }
    }


    /** Função para manipular a resposta de linhas de ônibus caso a resposta seja de sucesso
     *  retorna Resource.Succes, caso não tenha entrado no if, retornar erro */
    private fun handleBusRoutersResponse(response: Response<List<BusRoute>>): Resource<List<BusRoute>> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun getBusStopByLineCode(lineCode: Int) {

        viewModelScope.launch {

            _uiStateMapFragment.update {
                it.copy(isLoading = true)
            }

            try {

                if (_authenticate.value!!) {

                    val response = apiRepository.getBusStopByLineCode(lineCode)

                    handleBusStopByLineCodeResponse(response)

                } else {

                    _uiStateMainActivity.update {
                        it.copy(message = "Erro no sistema, tente novamente")
                    }

                    authenticate()
                }
            } catch (t: Throwable) {
                _uiStateMainActivity.update {
                    it.copy(message = t.message.toString())
                }
            }
        }
    }

    private fun handleBusStopByLineCodeResponse(response: Response<List<BusStop>>) {

        if (response.isSuccessful) {
            response.body()?.let { busStopList ->

                _uiStateMapFragment.update {
                    it.copy(
                        currentBusStop = busStopList.ifEmpty {
                            emptyList()
                        }
                    )
                }

                _uiStateMainActivity.update {
                    it.copy(currentQuantityBusStop = busStopList.size)
                }
            }
        } else {

            _uiStateMainActivity.update {
                it.copy(message = "Não foi possivel carregar ônibus, tente novamente")
            }
        }
    }


    /** Função para realizar a autenticação da API OLHO VIVO
     * no try e catch realizo a requisição e caso a api seja autenticada com sucesso
     * armazeno a resposta em [_authenticate] e caso ocorra algum problema
     * atualizo a [_uiStateMapFragment] com a mensagem de erro dependendo do tipo de exception*/
    private fun authenticate() {

        _uiStateMapFragment.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            try {

                _authenticate.value = apiRepository.postAuthenticate()

                _uiStateMapFragment.update {
                    it.copy(isLoading = false)
                }

            } catch (t: Throwable) {

                when (t) {
                    is IOException -> {
                        _uiStateMainActivity.update {
                            it.copy(message = "SPTrans está fora do ar, tente novamento mais tarde")
                        }

                        _uiStateMapFragment.update {
                            it.copy(isLoading = false)
                        }
                    }
                    else -> {
                        _uiStateMainActivity.update {
                            it.copy(message = "Erro de conversão")
                        }

                        _uiStateMapFragment.update {
                            it.copy(isLoading = false)
                        }
                    }
                }

            }

        }
    }


    /** Função para salvar a linha, envio a [busRoute] para o room utilizando o repository do room, e atualizo o [_uiStateMapFragment]
     * com a mensagem que a linha foi salva */
    fun favoriteBusRoute(busRoute: BusRoute) {
        viewModelScope.launch {

            roomRepository.favoriteBusRoute(busRoute)

            _uiStateMainActivity.update {
                it.copy(message = "Linha: ${busRoute.firstNumbersPlacard}-${busRoute.secondPartPlacard}, salva nos favoritos com Sucesso!!")
            }
        }
    }


    /** Função para realizer a coleta de um fluxo de Flow do [roomRepository] com todas as linhas que estão salvas no favorito
     * no Room e passo esse valor para mutableLiveData [_favoritesBusRoute] */
    private suspend fun getFavoritesBusRoute() {

        roomRepository.getFavoritesBusRoutes().collect { busRouteList ->

            _uiStateMainActivity.update {
                it.copy(favoriteBuses = busRouteList)
            }

        }
    }


    /** Função para limpar as mensagens do [_uiStateMapFragment]*/
    fun clearMessages() {
        _uiStateMainActivity.update {
            it.copy(message = START_MESSAGE)
        }
    }


    /** Função para limpar o letreiro completo atual*/
    fun clearLineCode() {
        _uiStateMainActivity.update {
            it.copy(
                currentLineCod = START_CURRENT_LINE_CODE,
                currentQuantityBusStop = START_QUANTITY_BUS_STOP,
                currentQuantityBus = START_QUANTITY_BUS
            )
        }
        _uiStateMapFragment.update {
            it.copy(
                currentBuses = emptyList(),
                currentBusStop = emptyList()
            )
        }
    }


    /** Função para realizar a requisição de todos os ônibus que estão circulando e a filtragem dessa requisição,
     * abro um escopo de viewmodel e atualizo o [_uiStateMapFragment] para loading true, no try e catch antes de realizar a requisição
     * verifico se já estou logado, caso sim, a requisição é feita e enviada para classe [handleBusRoutersResponseAndFilter]
     * para fazer a manipulação da response filtrar e atualizar o [_uiStateMapFragment] com o novo valor de lista de onibus com relação a linha
     * caso seja pego algum throwable durante a execução atualizo o [_uiStateMapFragment] com mensagem de erro*/
    fun getBusRouteSelected(fullPlacard: String, routeWay: Int) {

        _uiStateMainActivity.update {
            it.copy(
                currentLineCod = fullPlacard,
                currentLineWay = routeWay
            )
        }

        viewModelScope.launch {

            _uiStateMapFragment.update {
                it.copy(isLoading = true)
            }

            try {
                if (_authenticate.value!!) {

                    val response = apiRepository.getAllBus()

                    handleBusRoutersResponseAndFilter(response, routeWay)

                    _uiStateMapFragment.update {
                        it.copy(isLoading = false)
                    }

                } else {

                    _uiStateMainActivity.update {
                        it.copy(message = "Verfique sua conexão, o aplicativo não funciona sem internet ")
                    }

                    authenticate()

                }
            } catch (t: Throwable) {

                _uiStateMainActivity.update {
                    it.copy(message = t.message.toString())
                }

            }

        }

    }


    /** Função para manipular o resultado de um Response, caso [response] seja de sucesso, utilizo do for in na relação de linha e ônibus
     *  e verifico qual linha tem o letreiro completo igual ao letreiro atual da [_uiStateMapFragment], quando forem iguais,
     *  crio uma lista para armazernar todos os ônibus dessa linha, após armazenar todos eles, atualizo o [_uiStateMapFragment], com o valor da [curentListBusWithRoute]
     *  finalizo o if retornando Resourcer.Sucess, caso não tenha entrado no if retorno Resource.Error*/
    private fun handleBusRoutersResponseAndFilter(
        response: Response<ResponseAllBus>,
        routeWay: Int
    ) {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                //Para saber se a linha que está sendo buscada, tem algum ônibus em circulação
                var lineSearchExist = false
                val currentListBusWithRoute: MutableList<BusWithLine> = mutableListOf()

                resultResponse.lineRelation.forEach { busRouteWithBus ->
                    if (busRouteWithBus.fullPlacard == _uiStateMainActivity.value.currentLineCod && busRouteWithBus.lineWay == routeWay) {

                        lineSearchExist = true

                        busRouteWithBus.buses.forEach { bus ->

                            if (busRouteWithBus.lineWay == 1) {

                                val latLng = LatLng(bus.latBus, bus.longBus)

                                val busWithLine = BusWithLine(
                                    busRouteWithBus.fullPlacard,
                                    bus.prefixBus,
                                    busRouteWithBus.destinyPlacard,
                                    busRouteWithBus.originPlacard,
                                    bus.acessibleBus,
                                    resultResponse.hourGet,
                                    latLng,
                                    busRouteWithBus.lineCod
                                )

                                currentListBusWithRoute.add(busWithLine)

                            } else {

                                val latLng = LatLng(bus.latBus, bus.longBus)

                                val busWithLine = BusWithLine(
                                    busRouteWithBus.fullPlacard,
                                    bus.prefixBus,
                                    busRouteWithBus.originPlacard,
                                    busRouteWithBus.destinyPlacard,
                                    bus.acessibleBus,
                                    resultResponse.hourGet,
                                    latLng,
                                    busRouteWithBus.lineCod
                                )

                                currentListBusWithRoute.add(busWithLine)
                            }
                        }

                        _uiStateMapFragment.update {
                            it.copy(
                                isLoading = false,
                                currentBuses = currentListBusWithRoute
                            )
                        }

                        _uiStateMainActivity.update {
                            it.copy(currentQuantityBus = busRouteWithBus.amountBusFound)
                        }

                    }
                }

                if (!lineSearchExist) {
                    _uiStateMainActivity.update {
                        it.copy(message = "A linha procurada não tem ônibus em circulação no momento")
                    }
                }
            }
        } else {
            _uiStateMainActivity.update {
                it.copy(message = "Não foi possivel carregar ônibus, tente novamente")
            }
        }
    }


    fun getBusArrivalForecastByBusStop(busStopCod: Int) {
        viewModelScope.launch {

            _uiStateBusStopDialogFragment.update {
                it.copy(isLoading = true)
            }

            try {
                if (_authenticate.value!!) {

                    val response = apiRepository.getBusArrivalForecastByBusStop(busStopCod)

                    handleBusArrivalForecast(response)


                } else {

                    _uiStateBusStopDialogFragment.update {
                        it.copy(message = "Verfique sua conexão, o aplicativo não funciona sem internet ")
                    }

                    authenticate()

                }
            } catch (t: Throwable) {

            }
        }
    }

    private fun handleBusArrivalForecast(response: Response<ResponseBusArrivalForecast>) {
        if (response.isSuccessful) {

            val currentListBusArrivalForecast: MutableList<BusWithArrivalForecast> = mutableListOf()

            response.body()?.let { responseBusArrivalForecast ->
                responseBusArrivalForecast.busStopWithLineAndBus.busRouteWithBus.forEach { busRoute ->
                    for (bus in busRoute.buses) {

                        if (busRoute.lineWay == 1) {

                            val busWithArrivalForecast = BusWithArrivalForecast(
                                busRoute.fullPlacard,
                                busRoute.originPlacard,
                                bus.busArrivalForecast!!,
                                bus.prefixBus,
                                bus.acessibleBus
                            )

                            currentListBusArrivalForecast.add(busWithArrivalForecast)

                        } else {

                            val busWithArrivalForecast = BusWithArrivalForecast(
                                busRoute.fullPlacard,
                                busRoute.destinyPlacard,
                                bus.busArrivalForecast!!,
                                bus.prefixBus,
                                bus.acessibleBus
                            )

                            currentListBusArrivalForecast.add(busWithArrivalForecast)
                        }
                    }

                }

                _uiStateBusStopDialogFragment.update {
                    it.copy(
                        isLoading = false,
                        currentBusesArrivalForecast = currentListBusArrivalForecast,
                        lastHourUpdate = responseBusArrivalForecast.hourRefer
                    )
                }
            }
        } else {

            _uiStateBusStopDialogFragment.update {
                it.copy(
                    isLoading = false,
                    message = "Não foi possivel carregar nenhuma previsão para essa parada"
                )
            }
        }
    }

    //Atualizar o estado de zoom do ônibus para true
    fun zoomBus() {
        _uiStateMapFragment.update {
            it.copy(zoomCurrentBuses = true)
        }
    }


    //Atualizar o estado de zoom de ônibus para false
    fun offZoomBus() {
        _uiStateMapFragment.update {
            it.copy(zoomCurrentBuses = false)
        }
    }

    //Atualizar o estado da posição do usuário e o zoom para true
    fun currentLocationUser(currentUserLocation: LatLng) {
        _uiStateMapFragment.update {
            it.copy(
                currentLocationUser = currentUserLocation,
                focusUser = true
            )
        }
    }

    //Atualizar o estado do zoom no usuário atual para falso
    fun offFocusUser() {
        _uiStateMapFragment.update {
            it.copy(focusUser = false)
        }
    }

    /** Função para limpar as mensagens do [_uiStateMapFragment]*/
    fun clearMessageBusArrivalUi() {
        _uiStateBusStopDialogFragment.update {
            it.copy(message = START_MESSAGE)
        }
    }


}

/** Data class para representar o estado de UI do Map Fragment */
data class UiStateMapFragment(
    val isLoading: Boolean = false,
    val currentBuses: List<BusWithLine> = emptyList(),
    val currentBusStop: List<BusStop> = emptyList(),
    val zoomCurrentBuses: Boolean = false,
    val currentLocationUser: LatLng = START_CURRENT_LOCATION_USER,
    val focusUser: Boolean = false
)

/** Data class para representar o estado de UI da Main Activity */
data class UiStateMainActivity(
    val message: String = START_MESSAGE,
    val currentLineCod: String = START_CURRENT_LINE_CODE,
    val currentLineWay: Int = START_LINE_WAY,
    val currentQuantityBus: Int = START_QUANTITY_BUS,
    val currentQuantityBusStop: Int = START_QUANTITY_BUS_STOP,
    val favoriteBuses: List<BusRoute> = emptyList()
)

/** Data class para representar o estado de UI da Main Activity */
data class UiStateBusStopDialogFragment(
    val isLoading: Boolean = false,
    val message: String = START_MESSAGE,
    val lastHourUpdate: String = START_HOUR_UPDATE,
    val currentBusesArrivalForecast: List<BusWithArrivalForecast> = emptyList()
)




