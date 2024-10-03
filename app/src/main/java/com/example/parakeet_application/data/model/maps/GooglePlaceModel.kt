package com.example.parakeet_application.data.model.maps

data class GooglePlaceModel(
    val businessStatus: String?,
    val geometry: GeometryModel?,
    val icon: String?,
    val name: String?,
    val obfuscatedType: List<Any>?,
    val photos: List<PhotoModel>?,
    val placeId: String?,
    val rating: Double?,
    val reference: String?,
    val scope: String?,
    val types: List<String>?,
    val userRatingsTotal: Int?,
    val vicinity: String?,
    @Transient
    var saved: Boolean?
)