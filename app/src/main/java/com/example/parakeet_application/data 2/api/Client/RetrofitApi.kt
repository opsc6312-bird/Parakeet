package com.example.parakeet_application.data.api.Client

import com.example.parakeet_application.data.model.mapsModel.GoogleResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RetrofitApi {
    @GET
    suspend fun getNearbyPlaces(@Url url: String): Response<GoogleResponseModel>
}