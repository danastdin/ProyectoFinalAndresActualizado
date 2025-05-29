package com.example.proyectofinalandres.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectofinalandres.presentation.modelo.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navigateBack: () -> Unit,
    onAddedToCart: () -> Unit
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var addingToCart by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // corrutina para cargar el producto
    LaunchedEffect(productId) {
        db.collection("products").document(productId).get()
            .addOnSuccessListener { snap ->
                product = snap.toObject(Product::class.java)?.copy(id = snap.id)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            product?.let { p ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = p.imageUrl,
                        contentDescription = p.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(p.name, fontSize = 24.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Precio: ${p.price} €", fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(p.description, fontSize = 16.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            addingToCart = true
                            auth.currentUser?.uid?.let { uid ->
                                db.collection("users")
                                    .document(uid)
                                    .collection("cart")
                                    .document(p.id)
                                    .set(
                                        mapOf(
                                            "productId" to p.id,
                                            "name" to p.name,
                                            "price" to p.price,
                                            "imageUrl" to p.imageUrl
                                        )
                                    )
                                    .addOnSuccessListener {
                                        addingToCart = false
                                        onAddedToCart()
                                    }
                                    .addOnFailureListener {
                                        addingToCart = false
                                        errorMessage = "Error al añadir al carrito"
                                    }
                            }
                        },
                        enabled = !addingToCart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (addingToCart) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Añadir al carrito")
                    }
                    errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Color.Red)
                    }
                }
            } ?: run {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}