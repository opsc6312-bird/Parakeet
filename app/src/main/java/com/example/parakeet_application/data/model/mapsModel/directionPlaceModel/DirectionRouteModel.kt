package com.example.parakeet_application.data.model.mapsModel.directionPlaceModel

data class DirectionRouteModel(
    var legs: List<DirectionLegModel>? = null,
    var polylineModel: DirectionPolylineModel? = null,
    var summary: String? = null
) {

}