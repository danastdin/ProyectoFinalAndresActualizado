package com.example.proyectofinalandres.presentation.modelo

data class Product(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val size: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = ""
)
