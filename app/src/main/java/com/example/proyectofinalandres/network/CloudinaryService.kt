package com.example.proyectofinalandres.network

import com.example.proyectofinalandres.network.CloudinaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CloudinaryService {
    /**
     * Sube una imagen a Cloudinary (unsigned).
     * El campo "upload_preset" lo configuras en tu consola de Cloudinary
     * como un _unsigned upload preset_.
     */
    @Multipart
    @POST("image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryResponse
}
