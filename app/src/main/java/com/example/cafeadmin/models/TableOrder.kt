package com.example.cafeadmin.models

data class TableOrder(
    val tableNumber: String,
    val status: String,
    val total: Int,
    val items: List<Order>
)