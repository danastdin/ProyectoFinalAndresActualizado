package com.example.proyectofinalandres.network

import com.google.gson.annotations.SerializedName

/**
 * Mapea la respuesta de Cloudinary tras subir la imagen.
 * Solo incluimos lo necesario (la URL segura).
 */
data class CloudinaryResponse(
    @SerializedName("secure_url")
    val secureUrl: String
)