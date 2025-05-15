package com.example.cafeadmin.models

data class Food(
    var id: String = "",
    var name: String = "",
    var price: String = "0",
    var category: String = "",
    var imageUrl: String = "",
    var count: Int = 0
)
