// app/src/main/java/com/example/proyectofinalandres/presentation/product/ProductDetailScreen.kt
package com.example.proyectofinalandres.presentation.product

import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectofinalandres.presentation.modelo.Product
import com.example.proyectofinalandres.presentation.modelo.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

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
    var user by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var addingToCart by remember { mutableStateOf(false) }

    // 1) Cargo el producto *desde servidor* (evito caché local)
    LaunchedEffect(productId) {
        db.collection("products")
            .document(productId)
            .get(Source.SERVER)
            .addOnSuccessListener { snap ->
                product = snap.toObject(Product::class.java)?.copy(id = snap.id)
                Log.d("ProductDetail", "Producto fetched: $product")
            }
            .addOnFailureListener { e ->
                Log.e("ProductDetail", "Error al leer producto", e)
            }
    }

    // 2) Cargo el usuario actual *desde servidor* para comprobar isAdmin
    LaunchedEffect(auth.currentUser?.uid) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .get(Source.SERVER)
                .addOnSuccessListener { snap ->
                    val fetchedUser = snap.toObject(User::class.java)
                    Log.d("ProductDetail", "Usuario fetched: $fetchedUser")
                    user = fetchedUser
                }
                .addOnFailureListener { e ->
                    Log.e("ProductDetail", "Error al leer usuario", e)
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product?.name ?: "", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            product?.let { p ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = p.imageUrl,
                        contentDescription = p.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Precio: ${p.price} €", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = p.description, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón "Añadir al carrito"
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
                        if (addingToCart) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(text = "Añadir al carrito")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3) Solo si user?.isAdmin == true, muestro "Borrar producto"
                    if (user?.isAdmin == true) {
                        OutlinedButton(
                            onClick = {
                                db.collection("products")
                                    .document(p.id)
                                    .delete()
                                    .addOnSuccessListener { navigateBack() }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Error al borrar producto"
                                        Log.e("ProductDetail", "Error al borrar producto", e)
                                    }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor   = Color.Red
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Borrar producto")
                        }
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, color = Color.Red)
                    }
                }
            } ?: run {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
