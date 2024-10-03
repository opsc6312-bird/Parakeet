package com.example.parakeet_application.data.model.maps

data class GoogleResponseModel(
    val googlePlaceModelList: List<GooglePlaceModel>?,
    val error: String?
)
