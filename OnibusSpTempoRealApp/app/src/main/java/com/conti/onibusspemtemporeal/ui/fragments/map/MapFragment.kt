package com.conti.onibusspemtemporeal.ui.fragments.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.conti.onibusspemtemporeal.R
import com.conti.onibusspemtemporeal.data.models.BusStop
import com.conti.onibusspemtemporeal.data.models.BusWithLine
import com.conti.onibusspemtemporeal.databinding.FragmentMapBinding
import com.conti.onibusspemtemporeal.ui.adapter.MarkerInfoWindowBusAdapter
import com.conti.onibusspemtemporeal.ui.adapter.MarkerInfoWindowBusStopAdapter
import com.conti.onibusspemtemporeal.ui.viewModel.OnibusSpViewModel
import com.conti.onibusspemtemporeal.util.maps.BitmapHelper
import com.conti.onibusspemtemporeal.util.maps.BusRenderer
import com.conti.onibusspemtemporeal.util.maps.BusStopRenderer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.collections.MarkerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private val viewModel: OnibusSpViewModel by activityViewModels()


    private val iconCurrentLocation: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(
            requireContext(),
            R.color.black
        )
        BitmapHelper.vectorToBitmap(
            requireContext(),
            R.drawable.ic_user_location,
            color
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMapBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(
            R.id.map_fragment_container
        ) as? SupportMapFragment

        mapFragment?.getMapAsync(this)

        return binding.root
    }


    override fun onMapReady(googleMap: GoogleMap) {

        val markManager = MarkerManager(googleMap)

        val clusterManagerBuses =
            ClusterManager<BusWithLine>(requireContext(), googleMap, markManager)

        val clusterManagerBusStop =
            ClusterManager<BusStop>(requireContext(), googleMap, markManager)

        val currentUserMarker = googleMap.addMarker(
            MarkerOptions().position(LatLng(0.0, 0.0)).icon(iconCurrentLocation)
        )

        currentUserMarker!!.isVisible = false

        setupClusters(googleMap, clusterManagerBuses, clusterManagerBusStop)

        observerUiState(clusterManagerBuses, clusterManagerBusStop, googleMap, currentUserMarker)

        googleMap.setOnCameraIdleListener {
            clusterManagerBuses.onCameraIdle()
            clusterManagerBusStop.onCameraIdle()
        }
    }

    /** Função para fazer o setup dos clusters, do ônibus e parada.
     *  Chamada de três funções para complementar todas as funcionalidades do cluster
     *  setup widow adapter, renderers e click listener for widows */
    private fun setupClusters(
        googleMap: GoogleMap,
        clusterManagerBuses: ClusterManager<BusWithLine>,
        clusterManagerBusStop: ClusterManager<BusStop>
    ) {

        setupWindowAdapter(clusterManagerBuses, clusterManagerBusStop)

        setupRenderer(clusterManagerBuses, clusterManagerBusStop, googleMap)

        setupWidowClickListener(clusterManagerBuses, clusterManagerBusStop)
    }

    /** Função para criar o widow Adapter um para cada cluster, utilizando o
     *  [MarkerInfoWindowBusAdapter] para ônibus e [MarkerInfoWindowBusStopAdapter] para as paradas */
    private fun setupWindowAdapter(
        clusterManagerBuses: ClusterManager<BusWithLine>,
        clusterManagerBusStop: ClusterManager<BusStop>
    ) {

        clusterManagerBusStop.markerCollection.setInfoWindowAdapter(
            MarkerInfoWindowBusStopAdapter(
                requireContext()
            )
        )

        clusterManagerBuses.markerCollection.setInfoWindowAdapter(
            MarkerInfoWindowBusAdapter(
                requireContext()
            )
        )

    }

    /** Função para criar os renders de cada item do cluster
     * [BusRenderer] para ônibus e [BusStopRenderer] para as paradas */
    private fun setupRenderer(
        clusterManagerBuses: ClusterManager<BusWithLine>,
        clusterManagerBusStop: ClusterManager<BusStop>,
        googleMap: GoogleMap
    ) {

        clusterManagerBuses.renderer =
            BusRenderer(requireContext(), googleMap, clusterManagerBuses)

        clusterManagerBusStop.renderer =
            BusStopRenderer(requireContext(), googleMap, clusterManagerBusStop)

    }

    /** Função para criar um listener do click dos widows */
    private fun setupWidowClickListener(
        clusterManagerBuses: ClusterManager<BusWithLine>,
        clusterManagerBusStop: ClusterManager<BusStop>
    ) {

        clusterManagerBuses.setOnClusterItemInfoWindowClickListener { busWithLine ->

        }

        clusterManagerBusStop.setOnClusterItemInfoWindowClickListener { busStop ->
            val bundle = bundleOf("stop" to busStop.stopCod, "name" to busStop.stopName)
            findNavController().navigate(
                R.id.busStopDialogFragment,
                bundle
            )
        }

    }

    /** Função para observar o estado da Ui no viewModel com todos os dados necessários para atualizar a Ui*/
    private fun observerUiState(
        clusterManagerBuses: ClusterManager<BusWithLine>,
        clusterManagerBusStop: ClusterManager<BusStop>,
        googleMap: GoogleMap,
        currentUserMarker: Marker
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiStateMapFragment.collect { uiState ->

                    updateClusterBuses(
                        uiState.currentBuses,
                        clusterManagerBuses,
                    )

                    updateClusterBusStop(
                        uiState.currentBusStop,
                        clusterManagerBusStop
                    )

                    binding.progressBarLoadingBus.isVisible = uiState.isLoading

                    when {
                        uiState.currentBuses.isNotEmpty() && uiState.zoomCurrentBuses && !uiState.focusUser -> {

                            val firstBusLatLng = uiState.currentBuses.first().latLng

                            val zoom = if (uiState.currentBuses.size < 10) {
                                15f
                            } else {
                                10f
                            }

                            val cameraPosition = CameraPosition.Builder()
                                .target(firstBusLatLng)
                                .zoom(zoom)
                                .build()

                            googleMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    cameraPosition
                                )
                            )

                            viewModel.offZoomBus()
                        }

                        uiState.currentLocationUser != LatLng(0.0, 0.0) && uiState.focusUser -> {
                            currentUserMarker.isVisible = true
                            currentUserMarker.title = "Posição atual, falta implementar circulo"
                            currentUserMarker.position = uiState.currentLocationUser

                            val cameraPosition = CameraPosition.Builder()
                                .target(uiState.currentLocationUser)
                                .zoom(18f)
                                .build()

                            googleMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    cameraPosition
                                )
                            )

                            viewModel.offFocusUser()
                        }
                    }
                }
            }
        }
    }


    private fun updateClusterBuses(
        currentBuses: List<BusWithLine>,
        clusterManagerBuses: ClusterManager<BusWithLine>,
    ) {
        clusterManagerBuses.clearItems()
        clusterManagerBuses.addItems(currentBuses)
        clusterManagerBuses.cluster()
    }

    private fun updateClusterBusStop(
        currentBusStop: List<BusStop>,
        clusterManagerBusStop: ClusterManager<BusStop>
    ) {
        clusterManagerBusStop.clearItems()
        clusterManagerBusStop.addItems(currentBusStop)
        clusterManagerBusStop.cluster()
    }

}