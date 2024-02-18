package com.example.foursquareplaces.data

import com.example.foursquareplaces.model.NetworkResult
import com.example.foursquareplaces.model.PlacesResponse
import kotlinx.coroutines.flow.Flow

interface FetchPlacesRepository {
    suspend fun searchPlaces(ll: String, limit: Int,query:String="",categories:String="") : Flow<NetworkResult<PlacesResponse>>
}