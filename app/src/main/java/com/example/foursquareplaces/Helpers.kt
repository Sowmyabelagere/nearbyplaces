package com.example.foursquareplaces

fun formatDistance(distance: Int?):String {
    return if ((distance ?: 0) <= 1000) {
        distance.toString().trim().plus(" M")
    } else {
        String.format("%.1f", distance?.div(1000F)).plus(" Km")
    }
}