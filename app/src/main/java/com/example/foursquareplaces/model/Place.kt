package com.example.foursquareplaces.model

import com.example.foursquareplaces.formatDistance

data class Place(
    val fsq_id:String?,
    val categories:List<Category>?,
    val distance:Int?,
    val location: Location?,
    val name:String?
){
    fun getCategoryName() = categories?.get(0)?.name?.trim().toString()

    fun getCategoryIconUrl():String? =  categories?.get(0)?.getIcon()

    fun distanceKm(): String = formatDistance(distance)
}
