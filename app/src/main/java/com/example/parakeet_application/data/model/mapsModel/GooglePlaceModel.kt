package com.example.parakeet_application.data.model.mapsModel

import com.google.gson.annotations.SerializedName

data class GooglePlaceModel(
    val businessStatus: String?,
    val geometry: GeometryModel?,
    val icon: String?,
    @SerializedName("name") val name: String?,
    val obfuscatedType: List<Any>?,
    val photos: List<PhotoModel>?,
    @SerializedName("place_id") val placeId: String?,
    val rating: Double?,
    val reference: String?,
    val scope: String?,
    val types: List<String>?,
    @SerializedName("user_ratings_total")val userRatingsTotal: Int?,
    val vicinity: String?,
    @Transient
    var saved: Boolean?
)