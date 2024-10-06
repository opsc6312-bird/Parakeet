package com.example.parakeet_application.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parakeet_application.data.api.Client.birdsResponse
import com.example.parakeet_application.data.model.Root2
import kotlinx.coroutines.launch
import java.lang.Exception

class BirdsApiViewModel: ViewModel() {

    private val _state = MutableLiveData(MyScreen())
    val state: LiveData<MyScreen> get() = _state

    public fun fetchBirds(latitudes: Double, longitudes: Double){
        viewModelScope.launch {
            try {
                val birdsResponse = birdsResponse.searchRegion(latitudes, longitudes)
                _state.value = _state.value?.copy(listOfBirds = birdsResponse)
                Log.i("ListOfBirds", birdsResponse.toString())
            }catch (e: Exception){
                _state.value = _state.value?.copy(
                    error = "Error fetching regions ${e.message}"
                )
                Log.i("errorRegionMsg", e.message.toString())
            }
        }
    }


}

data class MyScreen(
    var listOfBirds: List<Root2> = mutableListOf(),
    var error: String = ""
)