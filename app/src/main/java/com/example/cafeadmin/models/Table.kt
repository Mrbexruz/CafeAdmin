package com.example.cafeadmin.models

data class Table(
    var id : Int,
    var total : Int = 0,
    var number: String ="",
    var isOrderReceived: Boolean = false,
    var hasOrder: Boolean = false,
    val orders: List<Order> = listOf()  // Agar orders ArrayList bo'lsa, buni List bilan almashtiring
){
    constructor() : this(0, 0, "")
}
