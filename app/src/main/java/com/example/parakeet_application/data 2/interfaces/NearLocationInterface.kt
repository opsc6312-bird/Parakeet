package com.example.parakeet_application.data.interfaces

import com.example.parakeet_application.data.model.mapsModel.GooglePlaceModel

interface NearLocationInterface {
    fun onSaveClick(googlePlaceModel: GooglePlaceModel)
    fun onDirectionClick(googlePlaceModel: GooglePlaceModel)
}