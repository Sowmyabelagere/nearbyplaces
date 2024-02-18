package com.example.foursquareplaces.data

import com.example.foursquareplaces.model.PlacesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {

    @GET("places/search")
    suspend fun getPlaces(
        @Query("query") query: String="",
        @Query("categories") categories:String="",
        @Query("ll") latLong: String,
        @Query("radius") offset: Int = 100000,
        @Query("open_now") openNow: Boolean=true,
        @Query("limit") limit: Int
    ):Response<PlacesResponse>
}