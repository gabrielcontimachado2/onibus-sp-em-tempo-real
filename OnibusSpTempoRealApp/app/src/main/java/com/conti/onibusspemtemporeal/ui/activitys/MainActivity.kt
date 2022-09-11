package com.conti.onibusspemtemporeal.ui.activitys

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.conti.onibusspemtemporeal.R
import com.conti.onibusspemtemporeal.data.models.BusRoute
import com.conti.onibusspemtemporeal.databinding.ActivityMainBinding
import com.conti.onibusspemtemporeal.ui.fragments.route.RouteBusSearchDialogFragment
import com.conti.onibusspemtemporeal.ui.viewModel.OnibusSpViewModel
import com.conti.onibusspemtemporeal.util.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: OnibusSpViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        openSearchActivity()
        observerUiState()
        setupChipSelectCategory()
        setupChipQuantityBus()
        setupFloatingCurrentLocationButton()
        refresh()

    }

    /** Função para quando o chip de quantity bus receber um click, chama a função de da o zoom no onibus*/
    private fun setupChipQuantityBus() {
        binding.chipQuantityBus.setOnClickListener {
            viewModel.zoomBus()
        }
    }


    /** Função para realizar uma busca da linha selecionada caso o chip da linha sejá clicado
     * e aplicar o zoom ao primeiro ônibus dessa linha*/
    private fun setupChipSelectCategory() {
        binding.chipLineSelected.setOnClickListener {
            viewModel.getBusRouteSelected(binding.chipLineSelected.text.toString())
            viewModel.zoomBus()
        }
    }

    /** Função para atualizar os ônibus chamando a função getBus do viewModel*/
    private fun refresh() {
        binding.floatingRefreshBus.setOnClickListener {
            viewModel.getBus()
        }
    }


    /** Função para consumir um LiveData das linhas que foram favoritadas e salvas no Room,
     * ao clicar no floating button, criei um popupMenu e adiciono no popupMenu todos
     * os itens da lista de linhas que foram consumidas do live data, com o título sendo o letreiro completo da linha
     * e caso selecione alguma item do menu, envio pra função do viewModel, a linha selecionada*/
    private fun setupPopupmenuFavorite(favoriteBusRouteList: List<BusRoute>) {
        binding.floatingButtonFavorite.setOnClickListener {

            val popupMenu = PopupMenu(this, it)

            popupMenu.menu.clear()

            popupMenu.menuInflater.inflate(R.menu.menu_popup_historico, popupMenu.menu)

            favoriteBusRouteList.forEach { busRoute ->
                popupMenu.menu.add("${busRoute.firstNumbersPlacard}-${busRoute.secondPartPlacard}")
            }

            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->

                viewModel.getBusRouteSelected(item.toString())

                true
            }
        }
    }


    /** Função para observar o [uiState]
     *  para seguir as boas praticas e recomendações do google na forma de coletar um stateFlow,
     *  utilizei do lifecycleScope e o repeatOnLifecycle, caso o stateFlow [uiState] mude seu valor
     *  o chip da quantidade de onibus vai mudar, se tiver messagem de error mostrar ele em uma Toast
     *  e se tiver um codigo de linha, chamar setChip enviando o codigo da linha */
    private fun observerUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiStateMainActivity.collect { uiStateMainActivity ->

                    binding.chipQuantityBus.text = uiStateMainActivity.currentQuantityBus.toString()

                    when {
                        uiStateMainActivity.message.isNotEmpty() -> {
                            Toast.makeText(
                                this@MainActivity,
                                uiStateMainActivity.message,
                                Toast.LENGTH_LONG
                            ).show()

                            viewModel.clearMessages()
                        }

                        uiStateMainActivity.currentLineCod.isNotEmpty() -> {
                            setChipCurrentLine(uiStateMainActivity.currentLineCod)
                        }

                        uiStateMainActivity.favoriteBuses.isNotEmpty() -> {
                            setupPopupmenuFavorite(uiStateMainActivity.favoriteBuses)
                        }

                    }
                }
            }
        }
    }

    /** Função para configurar o Chip da Linha atual,
     * torna o chip visivel e com a [currentLineCod] coloco no .text do chip
     * caso o botao do close for clickado, chip fica invisvel e chamo duas funções do viewmodel
     * retirar a linha atual, e pedir pra buscar todos so ônibus*/
    private fun setChipCurrentLine(currentLineCod: String) {

        with(binding.chipLineSelected) {

            isVisible = true
            text = currentLineCod

            setOnCloseIconClickListener {
                it.isInvisible = true
                viewModel.clearLineCode()
                viewModel.getBus()
            }

        }
    }


    /** Função para abrir o dialog fragment de busca de linhas de onibus*/
    private fun openSearchActivity() {
        binding.searchBusLines.setOnClickListener {
            val routeBusSearchDialogFragment = RouteBusSearchDialogFragment()
            routeBusSearchDialogFragment.show(supportFragmentManager, "Dialog_route_bus")
        }
    }


    private fun enableMyLocation() {

        if (ContextCompat.checkSelfPermission(
                this,
                permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            currentLocationToViewModel()
            return
        }


        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permission.ACCESS_COARSE_LOCATION
            )
        ) {
            setupAlertDialog(LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                permission.ACCESS_FINE_LOCATION,
                permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun setupAlertDialog(requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.tittleDialog))
        builder.setMessage(getString(R.string.messageDialog))

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            requestPermissions(
                arrayOf(permission.ACCESS_FINE_LOCATION),
                requestCode
            )
            requestPermissions(
                arrayOf(permission.ACCESS_COARSE_LOCATION),
                requestCode
            )
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    @SuppressLint("MissingPermission")
    private fun currentLocationToViewModel() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    viewModel.currentLocationUser(latLng)
                }
            }
    }

    private fun setupFloatingCurrentLocationButton() {
        binding.floatingCurrentLocation.setOnClickListener {
            enableMyLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                permission.ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions,
                grantResults,
                permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            Toast.makeText(
                this,
                "Não é possivel utilizar essa função sem permissão da localização atual",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isPermissionGranted(
        grantPermissions: Array<String>, grantResults: IntArray,
        permission: String
    ): Boolean {
        for (i in grantPermissions.indices) {
            if (permission == grantPermissions[i]) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

}
