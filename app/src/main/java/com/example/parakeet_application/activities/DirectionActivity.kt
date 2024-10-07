package com.example.parakeet_application.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import com.google.maps.android.PolyUtil
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parakeet_application.R
import com.example.parakeet_application.R.id.bottomSheet
import com.example.parakeet_application.adapter.DirectionStepAdapter
import com.example.parakeet_application.adapter.InfoWindowAdapter
import com.example.parakeet_application.data.model.mapsModel.GoogleResponseModel
import com.example.parakeet_application.data.model.mapsModel.directionPlaceModel.DirectionLegModel
import com.example.parakeet_application.data.model.mapsModel.directionPlaceModel.DirectionResponseModel
import com.example.parakeet_application.data.model.mapsModel.directionPlaceModel.DirectionRouteModel
import com.example.parakeet_application.data.model.mapsModel.directionPlaceModel.DirectionStepModel
import com.example.parakeet_application.databinding.ActivityDirectionBinding
import com.example.parakeet_application.databinding.BottomSheetLayoutBinding
import com.example.parakeet_application.permissions.AppPermissions
import com.example.parakeet_application.utility.LoadingDialog
import com.example.parakeet_application.utility.State
import com.example.parakeet_application.viewModel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.PolyUtil.decode

class DirectionActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityDirectionBinding
    private var mGoogleMap: GoogleMap?= null
    private lateinit var appPermissions: AppPermissions
    private var isLocationPermissionOk: Boolean = false
    private var isTrafficEnable = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var bottomSheetLayoutBinding: BottomSheetLayoutBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var currentLocation: Location
    private var startLat: Double? = null
    private var endLat: Double? = null
    private var startLng: Double? = null
    private var endLng: Double? = null
    private lateinit var placeId: String
    private lateinit var adapterStep: DirectionStepAdapter
    private val locationViewModel: LocationViewModel by viewModels()
    private var permissionRequest = mutableListOf<String>()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var currentMarkerOptions: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDirectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.apply {
            endLat = getDoubleExtra("lat", 0.0)
            endLng = getDoubleExtra("lng", 0.0)
            placeId = getStringExtra("placeId")!!
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appPermissions = AppPermissions()
        loadingDialog = LoadingDialog(this)
        bottomSheetLayoutBinding = binding.bottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayoutBinding.root)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        adapterStep = DirectionStepAdapter()
        bottomSheetLayoutBinding.stepRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DirectionActivity)
            setHasFixedSize(false)
            adapter = this@DirectionActivity.adapterStep
        }
       val mapFragment = supportFragmentManager.findFragmentById(R.id.directionMap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        binding.enableTraffic.setOnClickListener {
            if (isTrafficEnable){
                mGoogleMap?.isTrafficEnabled = false
                isTrafficEnable = false
            }else{
                mGoogleMap?.isTrafficEnabled = true
                isTrafficEnable = true
            }
        }

        binding.travelMode.setOnCheckedChangeListener{_, checked ->
            if (checked != -1){
                when(checked){
                    R.id.btnChipDriving->getDirection("driving")
                    R.id.btnChipBike->getDirection("biking")
                    R.id.btnChipWalking->getDirection("walking")
                    R.id.btnChipTrain->getDirection("train")
                }

            }
        }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isLocationPermissionOk =
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                            && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                if (isLocationPermissionOk) {
                    setUpGoogleMap()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Location permission was denied",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        else
            super.onBackPressed()
    }
private fun getDirection(mode: String){
    if (isLocationPermissionOk){
        loadingDialog.startLoading()
        val url = "https://maps.googleapis.com/directions/json?origin=${currentLocation.latitude},${currentLocation.longitude}&destination=$endLat,$endLng&mode=$mode&key=${getString(R.string.API_KEY)}"
        lifecycleScope.launchWhenStarted {
            locationViewModel.getDirection(url).collect{
                when (it){
                    is State.Loading -> {
                        if (it.flag == true){
                            loadingDialog.startLoading()
                        }
                    }
                    is State.Failed -> {
                        loadingDialog.stopLoading()
                        Snackbar.make(
                            binding.root, it.error,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is State.Success -> {
                        loadingDialog.stopLoading()
                        clearUI()
                        val directionResponseModel: DirectionResponseModel = it.data as DirectionResponseModel
                        val routeModel: DirectionRouteModel = directionResponseModel.directionRouteModels!![0]

                        supportActionBar!!.title=routeModel.summary
                        val legModel: DirectionLegModel = routeModel.legs?.get(0)!!
                        binding.apply {
                            txtStartLocation.text = legModel.startAddress
                            txtEndLocation.text = legModel.endAddress
                        }
                        bottomSheetLayoutBinding.apply {
                            txtSheetTime.text = legModel.duration?.text
                            txtSheetDistance.text = legModel.distance?.text
                        }
                        mGoogleMap?.addMarker(
                            MarkerOptions()
                                .title("End Location")
                                .position(
                                    LatLng(legModel.endLocation?.lat!!,
                                        legModel.endLocation.lng!!)
                                )
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bird))
                        )

                        mGoogleMap?.addMarker(
                            MarkerOptions()
                                .title("Start Location")
                                .position(
                                    LatLng(legModel.startLocation?.lat!!,
                                        legModel.startLocation.lng!!)
                                )
                               // .icon(BitmapDescriptorFactory.fromResource(R.drawable.bird))
                        )
                        adapterStep.setDirectionStepModels(legModel.steps!!)
                        val stepList: MutableList<LatLng> = ArrayList()
                        val options = PolylineOptions().apply {
                            width(25f)
                            color(Color.BLUE)
                            geodesic(true)
                            clickable(true)
                            visible(true)
                        }
                        val pattern: List<PatternItem>
                        if(mode == "walking"){
                            pattern = listOf(
                                Gap(10f),
                                Dot()
                            )
                            options.jointType(JointType.ROUND)
                        } else {
                            pattern = listOf(
                                Dash(30f)
                            )
                        }
                        options.pattern(pattern)
                        for (stepModel in legModel.steps) {
                            val decodedList = decode(stepModel.polyline?.points!!)
                            for (latLng in decodedList) {
                                stepList.add(
                                    LatLng(
                                        latLng.latitude,
                                        latLng.longitude
                                    )
                                )
                            }
                        }
                        options.addAll(stepList)

                        mGoogleMap?.addPolyline(options)
                        val startLocation = LatLng(
                            legModel.startLocation?.lat!!,
                            legModel.startLocation.lng!!
                        )
                        val endLocation = LatLng(
                            legModel.endLocation?.lat!!,
                            legModel.endLocation.lng!!
                        )
                        val builder = LatLngBounds.builder()
                        builder.include(endLocation).include(startLocation)
                        val latLngBounds = builder.build()
                        mGoogleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(
                                latLngBounds, 0
                            )
                        )
                    }

                }
            }
        }
    }

}

    private fun clearUI() {
        mGoogleMap?.clear()
        binding.txtStartLocation.text = ""
        binding.txtEndLocation.text = ""
        supportActionBar!!.title = ""
        bottomSheetLayoutBinding.txtSheetDistance.text=""
       bottomSheetLayoutBinding.txtSheetTime.text=""
    }

    override fun onMapReady(googleMap: GoogleMap) {
       mGoogleMap = googleMap
        when {
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionOk = true
                setUpGoogleMap()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("Parakeet is requesting access to the location permission")
                    .setPositiveButton("Ok") { _, _ ->
                        requestLocation()
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
        permissionLauncher.launch(permissionRequest.toTypedArray())
    }

    private fun setUpGoogleMap() {
        if (checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk= false
            return
        }
        mGoogleMap?.isMyLocationEnabled = true
        mGoogleMap?.uiSettings?.isTiltGesturesEnabled = true
        mGoogleMap?.uiSettings?.isMyLocationButtonEnabled = false
        mGoogleMap?.uiSettings?.isCompassEnabled = false
        getCurrentLocation()
    }
    private fun getCurrentLocation(){
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@DirectionActivity)
        if (checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
           isLocationPermissionOk = false
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it != null){
                currentLocation = it
                getDirection("driving")
            }else{
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()
    }

}