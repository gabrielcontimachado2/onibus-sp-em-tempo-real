package com.conti.onibusspemtemporeal.ui.fragments.busStop


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.conti.onibusspemtemporeal.R
import com.conti.onibusspemtemporeal.databinding.FragmentBusStopBinding
import com.conti.onibusspemtemporeal.ui.adapter.BusArrivalForecastAdapter
import com.conti.onibusspemtemporeal.ui.viewModel.OnibusSpViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BusStopDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentBusStopBinding
    private val viewModel: OnibusSpViewModel by activityViewModels()
    private val args: BusStopDialogFragmentArgs by navArgs()
    private lateinit var recyclerViewBusArrivalForecast: RecyclerView
    private lateinit var busArrivalForecastAdapter: BusArrivalForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

    }

    override fun onStart() {
        super.onStart()
        val d = dialog
        if (d != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            d.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBusStopBinding.inflate(inflater, container, false)

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBusStopCod()
        backHome()
        setupRecyclerView()
        observerUiState()
    }

    private fun setupBusStopCod() {
        Toast.makeText(requireContext(), "teste ${args.stop}", Toast.LENGTH_LONG).show()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.getBusArrivalForecastByBusStop(args.stop)
            }
        }
    }

    private fun observerUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiStateBusStopDialogFragment.collect { uiState ->

                    binding.progressBarLoadingBusArrivalForecast.isVisible = uiState.isLoading

                    when {
                        uiState.message.isNotEmpty() -> {
                            Toast.makeText(
                                requireContext(),
                                uiState.message,
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.clearMessageBusArrivalUi()
                        }
                        uiState.currentBusesArrivalForecast.isNotEmpty() -> {
                            busArrivalForecastAdapter.submitList(uiState.currentBusesArrivalForecast)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerViewBusArrivalForecast = binding.recylerViewBusArrivalForecast
        busArrivalForecastAdapter = BusArrivalForecastAdapter()
        recyclerViewBusArrivalForecast.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = busArrivalForecastAdapter
        }
    }

    /**Função para voltar para fechar o dialog */
    private fun backHome() {
        binding.imageButtonBackHome.setOnClickListener {
            dismiss()
        }
    }

}