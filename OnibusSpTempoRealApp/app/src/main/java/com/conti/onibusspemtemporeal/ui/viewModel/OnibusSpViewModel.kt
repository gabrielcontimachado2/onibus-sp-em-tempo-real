package com.conti.onibusspemtemporeal.ui.viewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conti.onibusspemtemporeal.data.models.BusRoute
import com.conti.onibusspemtemporeal.data.models.BusStop
import com.conti.onibusspemtemporeal.data.models.BusWithLine
import com.conti.onibusspemtemporeal.data.models.ResponseAllBus
import com.conti.onibusspemtemporeal.domain.apiRepository.OlhoVivoApiRepository
import com.conti.onibusspemtemporeal.domain.roomRepository.RoomRepository
import com.conti.onibusspemtemporeal.util.Constants.START_CURRENT_LINE_CODE
import com.conti.onibusspemtemporeal.util.Constants.START_CURRENT_LOCATION_USER
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


    init {

        //Por falta de tempo ainda n??o coloquei um refresh na autentica????o por uma quantidade de tempo
        authenticate()

        viewModelScope.launch {
            getFavoritesBusRoute()
        }

    }


    /** Fun????o para realizar requisi????o da posi????o de todos os ??nibus, coloco [_uiStateMapFragment] como loading true, e realizo dentro do try e catch,
     * a requisi????o utilizando o repositorio da api, utilizo da classe [handleBusRoutersResponse] para manipular a resposta e atualizar ela para
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

    /** Fun????o para manipular uma resposta de linha em rela????o aos ??nibus, caso a resposta for de sucesso
     * crio uma vari??vel para armazenar a linha e os ??nibus em circula????o delas, ap??s armazernas todos os ??nibus
     * atualizo o [_uiStateMapFragment] com essa lista de onibus, e para finalizar a fun????o retorno sucesso, caso n??o tenha entrado no if
     * finalizo a fun????o retornando erro */
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
                it.copy(message = "N??o foi possivel carregar ??nibus, tente novamente")
            }
        }
    }


    /** Fun????o para realizar uma requisi????o de GET na [apiRepository], consultar as linhas de onibus
     * disponiveis de acordo com o que foi digitado pelo usu??rio e armazenado na, [busRoute],
     * ao come??ar a fun????o [_busRoute] recebe estado de Loading, e em um try e catch realizo a requisi????o e atualizo [_busRoute] com o resultado da
     * lista de linhas disponiveis ou caso n??o tenha disponivel ou ocorra algum problema na requisi????o,
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
                        it.copy(message = "Verfique sua conex??o, o aplicativo n??o funciona sem internet ")
                    }
                }
            } catch (t: Throwable) {
                _uiStateMainActivity.update {
                    it.copy(message = t.message.toString())
                }
            }
        }
    }


    /** Fun????o para manipular a resposta de linhas de ??nibus caso a resposta seja de sucesso
     *  retorna Resource.Succes, caso n??o tenha entrado no if, retornar erro */
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
                it.copy(message = "N??o foi possivel carregar ??nibus, tente novamente")
            }
        }
    }


    /** Fun????o para realizar a autentica????o da API OLHO VIVO
     * no try e catch realizo a requisi????o e caso a api seja autenticada com sucesso
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
                            it.copy(message = "SPTrans est?? fora do ar, tente novamento mais tarde")
                        }

                        _uiStateMapFragment.update {
                            it.copy(isLoading = false)
                        }
                    }
                    else -> {
                        _uiStateMainActivity.update {
                            it.copy(message = "Erro de convers??o")
                        }

                        _uiStateMapFragment.update {
                            it.copy(isLoading = false)
                        }
                    }
                }

            }

        }
    }


    /** Fun????o para salvar a linha, envio a [busRoute] para o room utilizando o repository do room, e atualizo o [_uiStateMapFragment]
     * com a mensagem que a linha foi salva */
    fun favoriteBusRoute(busRoute: BusRoute) {
        viewModelScope.launch {

            roomRepository.favoriteBusRoute(busRoute)

            _uiStateMainActivity.update {
                it.copy(message = "Linha: ${busRoute.firstNumbersPlacard}-${busRoute.secondPartPlacard}, salva nos favoritos com Sucesso!!")
            }
        }
    }


    /** Fun????o para realizer a coleta de um fluxo de Flow do [roomRepository] com todas as linhas que est??o salvas no favorito
     * no Room e passo esse valor para mutableLiveData [_favoritesBusRoute] */
    private suspend fun getFavoritesBusRoute() {

        roomRepository.getFavoritesBusRoutes().collect { busRouteList ->

            _uiStateMainActivity.update {
                it.copy(favoriteBuses = busRouteList)
            }

        }
    }


    /** Fun????o para limpar as mensagens do [_uiStateMapFragment]*/
    fun clearMessages() {
        _uiStateMainActivity.update {
            it.copy(message = START_MESSAGE)
        }
    }


    /** Fun????o para limpar o letreiro completo atual*/
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


    /** Fun????o para realizar a requisi????o de todos os ??nibus que est??o circulando e a filtragem dessa requisi????o,
     * abro um escopo de viewmodel e atualizo o [_uiStateMapFragment] para loading true, no try e catch antes de realizar a requisi????o
     * verifico se j?? estou logado, caso sim, a requisi????o ?? feita e enviada para classe [handleBusRoutersResponseAndFilter]
     * para fazer a manipula????o da response filtrar e atualizar o [_uiStateMapFragment] com o novo valor de lista de onibus com rela????o a linha
     * caso seja pego algum throwable durante a execu????o atualizo o [_uiStateMapFragment] com mensagem de erro*/
    fun getBusRouteSelected(fullPlacard: String) {

        _uiStateMainActivity.update {
            it.copy(currentLineCod = fullPlacard)
        }

        viewModelScope.launch {

            _uiStateMapFragment.update {
                it.copy(isLoading = true)
            }

            try {
                if (_authenticate.value!!) {

                    val response = apiRepository.getAllBus()

                    handleBusRoutersResponseAndFilter(response)

                    _uiStateMapFragment.update {
                        it.copy(isLoading = false)
                    }

                } else {

                    _uiStateMainActivity.update {
                        it.copy(message = "Verfique sua conex??o, o aplicativo n??o funciona sem internet ")
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


    /** Fun????o para manipular o resultado de um Response, caso [response] seja de sucesso, utilizo do for in na rela????o de linha e ??nibus
     *  e verifico qual linha tem o letreiro completo igual ao letreiro atual da [_uiStateMapFragment], quando forem iguais,
     *  crio uma lista para armazernar todos os ??nibus dessa linha, ap??s armazenar todos eles, atualizo o [_uiStateMapFragment], com o valor da [curentListBusWithRoute]
     *  finalizo o if retornando Resourcer.Sucess, caso n??o tenha entrado no if retorno Resource.Error*/
    private fun handleBusRoutersResponseAndFilter(response: Response<ResponseAllBus>) {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                //Para saber se a linha que est?? sendo buscada, tem algum ??nibus em circula????o
                var lineSearchExist = false

                for (line in resultResponse.lineRelation) {
                    if (line.fullPlacard == _uiStateMainActivity.value.currentLineCod) {

                        lineSearchExist = true

                        val currentListBusWithRoute: MutableList<BusWithLine> = mutableListOf()

                        line.buses.forEach { bus ->

                            val latLng = LatLng(bus.latBus, bus.longBus)

                            val busWithLine = BusWithLine(
                                line.fullPlacard,
                                bus.prefixBus,
                                line.originPlacard,
                                line.destinyPlacard,
                                bus.acessibleBus,
                                resultResponse.hourGet,
                                latLng,
                                line.lineCod
                            )

                            currentListBusWithRoute.add(busWithLine)
                        }

                        _uiStateMapFragment.update {
                            it.copy(
                                isLoading = false,
                                currentBuses = currentListBusWithRoute
                            )
                        }

                        _uiStateMainActivity.update {
                            it.copy(currentQuantityBus = line.amountBusFound)
                        }
                    }
                }

                if (!lineSearchExist) {
                    _uiStateMainActivity.update {
                        it.copy(message = "A linha procurada n??o tem ??nibus em circula????o no momento")
                    }
                }
            }
        } else {
            _uiStateMainActivity.update {
                it.copy(message = "N??o foi possivel carregar ??nibus, tente novamente")
            }
        }
    }

    //Atualizar o estado de zoom do ??nibus para true
    fun zoomBus() {
        _uiStateMapFragment.update {
            it.copy(zoomCurrentBuses = true)
        }
    }


    //Atualizar o estado de zoom de ??nibus para false
    fun offZoomBus() {
        _uiStateMapFragment.update {
            it.copy(zoomCurrentBuses = false)
        }
    }

    //Atualizar o estado da posi????o do usu??rio e o zoom para true
    fun currentLocationUser(currentUserLocation: LatLng) {
        _uiStateMapFragment.update {
            it.copy(
                currentLocationUser = currentUserLocation,
                focusUser = true
            )
        }
    }

    //Atualizar o estado do zoom no usu??rio atual para falso
    fun offFocusUser() {
        _uiStateMapFragment.update {
            it.copy(focusUser = false)
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
    val currentQuantityBus: Int = START_QUANTITY_BUS,
    val currentQuantityBusStop: Int = START_QUANTITY_BUS_STOP,
    val favoriteBuses: List<BusRoute> = emptyList()
)





