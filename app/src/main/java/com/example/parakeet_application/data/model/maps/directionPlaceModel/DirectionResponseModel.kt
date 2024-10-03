package com.example.parakeet_application.data.model.maps.directionPlaceModel

data class DirectionResponseModel(
    var directionRouteModels: List<DirectionRouteModel>? = null,
    val error: String? = null
)