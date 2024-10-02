package com.example.parakeet_application.data.constants

import com.example.parakeet_application.R
import com.example.parakeet_application.data.model.PlacesModel

class AppConstant {
    companion object {
        @JvmStatic
        val STORAGE_REQUEST_CODE = 1000

        @JvmStatic
        val PROFILE_PATH = "/Profile/image_profile.jpg"

        const val LOCATION_REQUEST_CODE = 2000


        @JvmStatic
        val placesName =
            listOf<PlacesModel>(
                PlacesModel(1, R.drawable.ic_hotspot, "Hotspot", "hotspot"),
                PlacesModel(1, R.drawable.ic_hotspot, "Restaurant", "restaurant"),
                PlacesModel(2, R.drawable.ic_hotspot, "ATM", "atm"),
                PlacesModel(3, R.drawable.ic_hotspot, "Gas", "gas_station"),
                PlacesModel(4, R.drawable.ic_hotspot, "Groceries", "supermarket"),
                PlacesModel(5, R.drawable.ic_hotspot, "Hotels", "hotel"),
                PlacesModel(6, R.drawable.ic_hotspot, "Pharmacies", "pharmacy"),
                PlacesModel(7, R.drawable.ic_hotspot, "Hospitals & Clinics", "hospital"),
                PlacesModel(8, R.drawable.ic_hotspot, "Car Wash", "car_wash"),
                PlacesModel(9, R.drawable.ic_hotspot, "Beauty Salons", "beauty_salon")
            )
    }
}