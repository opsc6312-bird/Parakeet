package com.example.parakeet_application.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import com.example.parakeet_application.R
import com.example.parakeet_application.viewModel.LocationViewModel

class SettingsFragment : Fragment() {
    private lateinit var distanceUnitSwitch: SwitchCompat
    private lateinit var radioGroupDistance: RadioGroup
    private lateinit var viewModel: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LocationViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        distanceUnitSwitch = view.findViewById(R.id.distance_unit_switch)
        radioGroupDistance = view.findViewById(R.id.radio_group_distance)

        val sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val (isKilometers, maxDistance) = viewModel.getDistancePreferences(sharedPreferences)
        distanceUnitSwitch.isChecked = isKilometers

        when (maxDistance){
            5 -> radioGroupDistance.check(R.id.radio_5km)
            10 -> radioGroupDistance.check(R.id.radio_10km)
            20 -> radioGroupDistance.check(R.id.radio_20km)
            50 -> radioGroupDistance.check(R.id.radio_50km)
        }

        distanceUnitSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveDistancePreferences(sharedPreferences, isChecked, maxDistance)
        }
        radioGroupDistance.setOnCheckedChangeListener { _, checkedId ->
            val selectedDistance = when (checkedId) {
                R.id.radio_5km -> 5
                R.id.radio_10km -> 10
                R.id.radio_20km -> 20
                R.id.radio_50km -> 50
                else -> 5
            }
            viewModel.saveDistancePreferences(sharedPreferences, distanceUnitSwitch.isChecked, selectedDistance)
        }
        return view
    }
}