package com.example.cafeadmin.models

data class Order(
    val name: String,
    val price: Int,
    val count: Int,
    val imageUrl: String
)