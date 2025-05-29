package com.example.proyectofinalandres.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File

object CloudinaryClient {
    // Tus credenciales de Cloudinary
    private const val CLOUD_NAME    = "daz3lynmg"
    private const val UPLOAD_PRESET = "android_preset"
    private const val BASE_URL      = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val client = OkHttpClient()

    /**
     * Sube el [file] a Cloudinary usando un preset sin firmar.
     * Devuelve la URL segura (secure_url) o lanza excepción.
     */
    @Throws(Exception::class)
    suspend fun uploadImage(file: File): String {
        // Construye el multipart body
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), file))
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        // Construye la petición
        val request = Request.Builder()
            .url(BASE_URL)
            .post(body)
            .build()

        // Ejecuta en hilo de IO
        val response = client.newCall(request).execute()
        val text = response.body?.string() ?: throw Exception("No response body")

        if (!response.isSuccessful) {
            // lanza incluyendo el mensaje que devuelva Cloudinary
            val msg = try { JSONObject(text).optString("error") } catch (_: Exception) { text }
            throw Exception("Error al subir imagen: ${response.code} $msg")
        }

        // Extrae la URL segura
        val json = JSONObject(text)
        return json.getString("secure_url")
    }
}