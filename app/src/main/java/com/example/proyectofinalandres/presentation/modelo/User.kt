package com.example.proyectofinalandres.presentation.modelo

data class User(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val description: String = "",
    val image: String = "",
    val products: List<Product> = emptyList(),
    val birthDate: String = ""
)
