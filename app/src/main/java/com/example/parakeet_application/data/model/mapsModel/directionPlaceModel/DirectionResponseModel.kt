package com.example.parakeet_application.data.model.mapsModel.directionPlaceModel

data class DirectionResponseModel(
    var directionRouteModels: List<DirectionRouteModel>? = null,
    val error: String? = null
)