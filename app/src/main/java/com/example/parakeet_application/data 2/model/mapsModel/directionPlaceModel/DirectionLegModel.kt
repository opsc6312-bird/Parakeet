package com.example.parakeet_application.data.model.mapsModel.directionPlaceModel

data class DirectionLegModel(
    val distance: DirectionDistanceModel? = null,
    val duration: DirectionDurationModel? = null,
    val endAddress: String? = null,
    val endLocation: EndLocationModel? = null,
    val startAddress: String? = null,
    val startLocation: StartLocationModel? = null,
    val steps: List<DirectionStepModel>? = null
)