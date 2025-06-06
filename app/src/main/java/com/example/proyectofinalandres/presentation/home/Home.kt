package com.example.proyectofinalandres.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectofinalandres.R
import com.example.proyectofinalandres.presentation.modelo.Product
import com.example.proyectofinalandres.presentation.modelo.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var user by remember { mutableStateOf<User?>(null) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }

    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { snap -> user = snap.toObject(User::class.java) }
        }
        db.collection("products").get()
            .addOnSuccessListener { result ->
                products = result.documents.mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id) }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bienvenido", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Text(user?.name ?: "", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        if (!user?.image.isNullOrEmpty()) {
                            AsyncImage(
                                model = user!!.image,
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.user_icon),
                                contentDescription = "Perfil"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                IconButton(onClick = onHomeClick, Modifier.weight(1f)) {
                    Icon(painter = painterResource(id = R.drawable.home), contentDescription = "Home")
                }
                IconButton(onClick = onSearchClick, Modifier.weight(1f)) {
                    Icon(painter = painterResource(id = R.drawable.search_lens), contentDescription = "Buscar")
                }
                IconButton(onClick = onAddClick, Modifier.weight(1f)) {
                    Icon(painter = painterResource(id = R.drawable.add), contentDescription = "Añadir")
                }
                IconButton(onClick = onCartClick, Modifier.weight(1f)) {
                    Icon(painter = painterResource(id = R.drawable.shopping_cart), contentDescription = "Carrito")
                }
            }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // Imagen local con blur y transparencia
            Image(
                painter = painterResource(id = R.drawable.nvrmnd_fondo),
                contentDescription = "Fondo Home",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(2.dp)
                    .graphicsLayer { alpha = 0.3f }
            )

            // Contenido sobre el fondo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                Text(
                    "Productos añadidos recientemente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(products) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clickable { onProductClick(product.id) },
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
