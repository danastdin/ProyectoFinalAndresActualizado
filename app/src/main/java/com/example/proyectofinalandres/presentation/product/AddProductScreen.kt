package com.example.proyectofinalandres.presentation.product

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.proyectofinalandres.R
import com.example.proyectofinalandres.network.CloudinaryClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val pickImage = rememberLauncherForActivityResult(GetContent()) {
        imageUri = it
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Producto") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { inner ->
        Box(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.nvrmnd_fondo),
                contentDescription = "Fondo Add Product",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(2.dp)
                    .graphicsLayer { alpha = 0.3f }
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Marca *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio (€) *") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Talla") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable { pickImage.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("Toca para seleccionar imagen", color = Color.DarkGray)
                    }
                }

                Spacer(Modifier.weight(1f))

                error?.let {
                    Text(it, color = Color.Red, fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        if (name.isBlank() || brand.isBlank() || price.isBlank() || imageUri == null) {
                            error = "Nombre, marca, precio e imagen son obligatorios."
                            return@Button
                        }

                        uploading = true
                        error = null

                        scope.launch {
                            try {
                                val imageUrl = withContext(Dispatchers.IO) {
                                    val input = context.contentResolver.openInputStream(imageUri!!)!!
                                    val tmp = File(context.cacheDir, "upload.jpg")
                                    FileOutputStream(tmp).use { out -> input.copyTo(out) }
                                    CloudinaryClient.uploadImage(tmp)
                                }

                                val prod = mapOf(
                                    "name"        to name,
                                    "brand"       to brand,
                                    "price"       to price.toDouble(),
                                    "description" to description,
                                    "size"        to size,
                                    "imageUrl"    to imageUrl
                                )

                                db.collection("products")
                                    .add(prod)
                                    .addOnSuccessListener { navigateBack() }
                                    .addOnFailureListener { e -> error = e.localizedMessage }
                            } catch (e: Exception) {
                                Log.e("AddProduct", "Error al subir imagen", e)
                                error = "Error al subir imagen"
                            } finally {
                                uploading = false
                            }
                        }
                    },
                    enabled = !uploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (uploading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Guardar Producto")
                    }
                }
            }
        }
    }
}
