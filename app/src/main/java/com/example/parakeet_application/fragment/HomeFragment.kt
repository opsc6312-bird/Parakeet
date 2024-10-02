package com.example.parakeet_application.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.example.parakeet_application.R
import com.example.parakeet_application.data.constants.AppConstant
import com.example.parakeet_application.databinding.FragmentHomeBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.chip.Chip

class HomeFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var appPermission: RuntimePermission
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionRequest = mutableListOf<String>()
    private var isLocationPermissionOk: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPermission = AppPermissions()
        loadingDialog = LoadingDialog(requireContext())
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions -> isLocationPermissionOk= permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }
        val mapFragment = (childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)

        for(placeModel in AppConstant.placesName){
            val chip = Chip(requireContext())
            chip.text = placeModel.name
            chip.id = placeModel.id
            chip.setPadding(0, 0, 0, 0)
            chip.setTextColor(resources.getColor(R.color.white, null))
            chip.chipBackgroundColor = resources.getColorStateList(R.color.black, null)
            chip.chipIcon= ResourcesCompat.getDrawable(resources, placeModel.drawableId, null)
            chip.isClickable = true
            chip.isCheckedIconVisible = false
            binding.placesGroup.addView(chip)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mGoogleMap =mGoogleMap
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionOk = true
                setUpGoogleMap()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission")
                    .setMessage("Parakeet is requesting access to the location permission")
                    .setPositiveButton("Ok"){
                        _, _ -> requestLocation()
                    }.create().show()
            }
            else -> {
                requestLocation()
            }
        }
    }

    private fun requestLocation() {
        permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun setUpGoogleMap() {
        TODO("Not yet implemented")
    }
}