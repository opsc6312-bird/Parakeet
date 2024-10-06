package com.example.parakeet_application.data.api.Client

import com.example.parakeet_application.data.model.Root2
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

//Image generator image service
private  val retrofitImage = Retrofit.Builder().baseUrl("https://api.ebird.org/v2/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val birdsResponse = retrofitImage.create(ImageApiService::class.java)

interface ImageApiService{

    //https://api.ebird.org/v2/data/obs/geo/recent?lat={{lat}}&lng={{lng}}
    @Headers("X-eBirdApiToken: gi422aer30qs")
    @GET("data/obs/geo/recent")
    suspend fun searchRegion(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ) : List<Root2>
}