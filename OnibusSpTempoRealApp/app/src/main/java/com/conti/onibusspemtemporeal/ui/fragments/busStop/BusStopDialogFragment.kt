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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.conti.onibusspemtemporeal.R
import com.conti.onibusspemtemporeal.databinding.FragmentBusStopBinding
import com.conti.onibusspemtemporeal.ui.adapter.BusArrivalForecastAdapter
import com.conti.onibusspemtemporeal.ui.viewModel.OnibusSpViewModel
import com.google.android.material.chip.Chip
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
    ): View {

        binding = FragmentBusStopBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textViewStopNameStatic.text = args.name

        setupBusStopCod()
        backHome()
        setupRecyclerView()
        observerUiState()

    }

    /**Função para configurar o floating button de refresh*/
    private fun setupFloatingRefresh(checked: String) {
        binding.floatingRefreshBusArrivalForecast.setOnClickListener {
            if (checked == getString(R.string.all)) {
                viewModel.getBusArrivalForecastByBusStop(args.stop)
            } else {
                viewModel.getFilterBusArrivalForecastByBusStop(args.stop, checked)
            }
        }
    }

    /**Função para realizar a busca da previsão de ônibus dessa parada quando ela for a tela for criada */
    private fun setupBusStopCod() {
        viewModel.getBusArrivalForecastByBusStop(args.stop)
    }

    /** Função para observar a Ui State da tela [BusStopDialogFragment] */
    private fun observerUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStateBusStopDialogFragment.collect { uiState ->

                    binding.progressBarLoadingBusArrivalForecast.isVisible = uiState.isLoading
                    binding.textViewLastUpdate.text =
                        getString(R.string.lastUpdate, uiState.lastHourUpdate)

                    when {
                        uiState.message.isNotEmpty() -> {
                            Toast.makeText(
                                requireContext(),
                                uiState.message,
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.clearMessageBusArrivalUi()
                        }
                        uiState.currentListBusesArrivalForecast.isNotEmpty() -> {
                            busArrivalForecastAdapter.submitList(uiState.currentListBusesArrivalForecast)
                        }
                        uiState.checked.isNotEmpty() -> {
                            setupFloatingRefresh(uiState.checked)
                        }
                    }

                    setupChipGroup(uiState.currentListRoutePlacard, uiState.checked)
                }

            }
        }
    }

    /**Função para configurar o Chip Group das linhas de ônibus que vão passar nessa parada
     * A função recebe como parametro [currentListRoutePlacard] com a lista das linhas de ônibus dessa parada
     * [routePlacardChipGroup] variável para receber o view binding do chip group
     * [chip] variável para inflar o layout do chip, cria-lo e adiciona-lo na no chipGroup*/
    private fun setupChipGroup(currentListRoutePlacard: List<String>, checked: String) {
        val routePlacardChipGroup = binding.layoutChipGroup.chipGroupBusRoute

        with(routePlacardChipGroup) {
            removeAllViews()

            currentListRoutePlacard.forEachIndexed { index, fullPlacard ->

                val chip = layoutInflater.inflate(R.layout.single_chip, null) as Chip

                chip.text = fullPlacard
                addView(chip)

                if (chip.text == checked) {
                    routePlacardChipGroup.check(routePlacardChipGroup.getChildAt(index).id)
                }

                /** Check which Chip was Selected */
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (chip.text.toString() == getString(R.string.all)) {
                            viewModel.getBusArrivalForecastByBusStop(args.stop)
                        } else {
                            viewModel.getFilterBusArrivalForecastByBusStop(
                                args.stop,
                                chip.text.toString()
                            )
                        }
                    } else {
                        viewModel.getBusArrivalForecastByBusStop(args.stop)
                    }
                }
            }
        }
    }


    /**Função para configurar a recyclerView*/
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
