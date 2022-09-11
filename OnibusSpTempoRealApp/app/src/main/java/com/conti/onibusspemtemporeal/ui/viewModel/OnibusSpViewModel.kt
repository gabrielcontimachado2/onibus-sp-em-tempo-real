package com.conti.onibusspemtemporeal.ui.viewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conti.onibusspemtemporeal.data.models.BusRoute
import com.conti.onibusspemtemporeal.data.models.BusWithLine
import com.conti.onibusspemtemporeal.data.models.ResponseAllBus
import com.conti.onibusspemtemporeal.domain.apiRepository.OlhoVivoApiRepository
import com.conti.onibusspemtemporeal.domain.roomRepository.RoomRepository
import com.conti.onibusspemtemporeal.util.Constants.START_BUS_ROUTE
import com.conti.onibusspemtemporeal.util.Constants.START_CURRENT_LINE_CODE
import com.conti.onibusspemtemporeal.util.Constants.START_CURRENT_LOCATION_USER
import com.conti.onibusspemtemporeal.util.Constants.START_MESSAGE
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
            it.copy(currentLineCod = START_CURRENT_LINE_CODE)
        }
    }


    /** Função para realizar a requisição de todos os ônibus que estão circulando e a filtragem dessa requisição,
     * abro um escopo de viewmodel e atualizo o [_uiStateMapFragment] para loading true, no try e catch antes de realizar a requisição
     * verifico se já estou logado, caso sim, a requisição é feita e enviada para classe [handleBusRoutersResponseAndFilter]
     * para fazer a manipulação da response filtrar e atualizar o [_uiStateMapFragment] com o novo valor de lista de onibus com relação a linha
     * caso seja pego algum throwable durante a execução atualizo o [_uiStateMapFragment] com mensagem de erro*/
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
    private fun handleBusRoutersResponseAndFilter(response: Response<ResponseAllBus>) {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                //Para saber se a linha que está sendo buscada, tem algum ônibus em circulação
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

}

/** Data class para representar o estado de UI do Map Fragment */
data class UiStateMapFragment(
    val isLoading: Boolean = false,
    val currentBuses: List<BusWithLine> = emptyList(),
    val zoomCurrentBuses: Boolean = false,
    val currentLocationUser: LatLng = START_CURRENT_LOCATION_USER,
    val focusUser: Boolean = false
)

/** Data class para representar o estado de UI da Main Activity */
data class UiStateMainActivity(
    val message: String = START_MESSAGE,
    val currentLineCod: String = START_CURRENT_LINE_CODE,
    val currentQuantityBus: Int = START_BUS_ROUTE,
    val favoriteBuses: List<BusRoute> = emptyList()
)



