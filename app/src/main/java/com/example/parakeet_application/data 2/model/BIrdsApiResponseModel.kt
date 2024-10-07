package com.example.parakeet_application.data.model

data class Root2(
    val speciesCode: String,
    val comName: String,
    val sciName: String,
    val locId: String,
    val locName: String,
    val obsDt: String,
    val howMany: Long,
    val lat: Double,
    val lng: Double,
    val obsValid: Boolean,
    val obsReviewed: Boolean,
    val locationPrivate: Boolean,
    val subId: String,
)