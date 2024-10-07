package com.example.parakeet_application.activities

import android.location.Location
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.parakeet_application.R
import com.example.parakeet_application.R.id.bottomSheet
import com.example.parakeet_application.data.model.mapsModel.directionPlaceModel.DirectionStepModel
import com.example.parakeet_application.databinding.ActivityDirectionBinding
import com.example.parakeet_application.databinding.BottomSheetLayoutBinding
import com.example.parakeet_application.permissions.AppPermissions
import com.example.parakeet_application.utility.LoadingDialog
import com.example.parakeet_application.viewModel.LocationViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior

class DirectionActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityDirectionBinding
    private var mGoogleMap: GoogleMap?= null
    private lateinit var appPermissions: AppPermissions
    private var isLocationPermissionOk = false
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
    private lateinit var adapter: DirectionStepModel
    private val locationViewModel: LocationViewModel by viewModels()

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
        //bottomSheetLayoutBinding = binding.bottomSheetLayout;

    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        super.onBackPressed()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        TODO("Not yet implemented")
    }
}