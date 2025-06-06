package com.example.proyectofinalandres.presentation.modelo

import com.google.firebase.firestore.PropertyName

data class User(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val description: String = "",
    val image: String = "",
    val products: List<Any> = emptyList(),
    val birthDate: String = "",

    //Utilizo @get y @set property para que lea exactamente el mismo campo que en firebase.
    //Por alguna razón, cuando obtiene el atributo de mi colección users, no entrega correctamente
    //el campo isAdmin y lo tengo que mapear manualmente.
    @get:PropertyName("isAdmin")
    @set:PropertyName("isAdmin")
    var isAdmin: Boolean = false
)
