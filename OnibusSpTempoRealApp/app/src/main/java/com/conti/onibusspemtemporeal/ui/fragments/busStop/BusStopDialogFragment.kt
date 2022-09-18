package com.conti.onibusspemtemporeal.ui.fragments.busStop


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.conti.onibusspemtemporeal.R
import com.conti.onibusspemtemporeal.databinding.FragmentBusStopBinding

class BusStopDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentBusStopBinding

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

        backHome()
    }

    /**Função para voltar para fechar o dialog */
    private fun backHome() {
        binding.imageButtonBackHome.setOnClickListener {
            dismiss()
            onDestroyView()
        }
    }

}