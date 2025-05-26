package com.example.proyectofinalandres.presentation.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectofinalandres.presentation.modelo.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onAddedToCart: () -> Unit
) {
    var addingToCart by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = product.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Precio: ${product.price} €", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = product.description ?: "Sin descripción", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    addingToCart = true
                    errorMessage = null

                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val cartItem = hashMapOf(
                            "productId" to product.id,
                            "name" to product.name,
                            "price" to product.price,
                            "imageUrl" to product.imageUrl
                        )
                        db.collection("users")
                            .document(uid)
                            .collection("cart")
                            .add(cartItem)
                            .addOnSuccessListener {
                                addingToCart = false
                                onAddedToCart()
                            }
                            .addOnFailureListener {
                                addingToCart = false
                                errorMessage = "Error al añadir al carrito"
                            }
                    } else {
                        addingToCart = false
                        errorMessage = "Usuario no autenticado"
                    }
                },
                enabled = !addingToCart
            ) {
                if (addingToCart) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Añadir al carrito")
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage ?: "", color = Color.Red)
            }
        }
    }
}