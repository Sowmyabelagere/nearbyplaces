package com.example.foursquareplaces.data

import com.example.foursquareplaces.model.NetworkResult
import com.example.foursquareplaces.model.PlacesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FetchPlacesRepositoryImpl(private val placesApiService: PlacesApiService) :
    FetchPlacesRepository {

    override suspend fun searchPlaces(
        ll: String,
        limit: Int,
        query: String,
        categories: String
    ): Flow<NetworkResult<PlacesResponse>> {
        return flow {
            try {
                emit(NetworkResult.Loading())
                val result = placesApiService.getPlaces(latLong = ll, limit = limit, query = query, categories = categories)
                if(result.isSuccessful){
                    val responseHeaders = result.headers()
                    val link = responseHeaders["Link"]
                    println("Link${link}")
                    emit(NetworkResult.Success(result.body() as PlacesResponse))
                }else{
                   emit(NetworkResult.Error(result.message()))
                }
            }catch (ex:Exception){
                emit(NetworkResult.Error(ex.message))
            }
        }
    }
}