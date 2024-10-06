package com.example.parakeet_application.data.model.mapsModel

import com.google.gson.annotations.SerializedName

data class GoogleResponseModel(
    @SerializedName("results") val googlePlaceModelList: List<GooglePlaceModel>?,
    @SerializedName("error_message") val error: String?
)
