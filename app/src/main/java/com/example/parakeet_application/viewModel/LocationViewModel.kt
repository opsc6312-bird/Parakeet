package com.example.parakeet_application.viewModel

import androidx.lifecycle.ViewModel
import com.example.parakeet_application.repo.AppRepo
import kotlinx.coroutines.flow.Flow

class LocationViewModel: ViewModel() {
    private val repo  = AppRepo()
    fun getNearByPlaces(url: String) = repo.getPlaces(url)

}