package com.example.parakeet_application.data.model

data class SavedPlacesModel(
    var name:String="",
    var address:String="",
    var placeId:String="",
    var totalRating:Int=0,
    var rating:Double=0.0,
    var lat: Double=0.0,
    var lng:Double=0.0
)
