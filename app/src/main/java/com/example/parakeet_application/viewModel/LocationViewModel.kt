package com.example.parakeet_application.viewModel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parakeet_application.data.model.SavedPlacesModel
import com.example.parakeet_application.data.model.mapsModel.GooglePlaceModel
import com.example.parakeet_application.repo.AppRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationViewModel: ViewModel() {
    private val repo  = AppRepo()
    fun getNearByPlace(url: String) = repo.getPlaces(url)
    fun removePlace(userSavedLocationId: ArrayList<String>) = repo.removePlace(userSavedLocationId = userSavedLocationId)
    fun addUserPlace(googlePlaceModel: GooglePlaceModel, userSavedLocationId: ArrayList<String>) = repo.addUserPlace(googlePlaceModel = googlePlaceModel, userSavedLocationId = userSavedLocationId)
    fun getUserLocationId(): ArrayList<String>{
        var data: ArrayList<String> = ArrayList()
        viewModelScope.launch {
            data = withContext(Dispatchers.Default) { repo.getUserLocationId()}
        }
        return data
    }
    fun getDistancePreferences(sharedPreferences: SharedPreferences): Pair<Boolean, Int> {
        return repo.getDistanceUnitPreferences(sharedPreferences)
    }

    fun saveDistancePreferences(sharedPreferences: SharedPreferences, isKilometers: Boolean, maxDistance: Int) {
        repo.saveDistanceUnitPreferences(sharedPreferences, isKilometers, maxDistance)
    }
}