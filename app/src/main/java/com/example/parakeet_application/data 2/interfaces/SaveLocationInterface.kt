package com.example.parakeet_application.data.interfaces

import com.example.parakeet_application.data.model.SavedPlacesModel

interface SaveLocationInterface {
    fun onLocationClick(savedPlaceModel: SavedPlacesModel)
}