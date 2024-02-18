package com.example.foursquareplaces.model

data class Category(
    val id: String?,
    val name: String?,
    val icon: Icon?,
    val plural_name: String?,
    val short_name: String?
){
    fun getIcon(): String = icon?.prefix.plus("100").plus(icon?.suffix)
}
