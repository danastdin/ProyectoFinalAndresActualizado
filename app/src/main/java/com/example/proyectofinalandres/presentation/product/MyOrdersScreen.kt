package com.example.proyectofinalandres.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class OrderItem(
    val docId: String,
    val productId: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navigateBack: () -> Unit
) {
    val uid = auth.currentUser?.uid
    var orders by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .collection("orders")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { snaps ->
                    orders = snaps.documents.map { doc ->
                        OrderItem(
                            docId     = doc.id,
                            productId = doc.getString("productId") ?: "",
                            name      = doc.getString("name")      ?: "",
                            price     = doc.getDouble("price")     ?: 0.0,
                            imageUrl  = doc.getString("imageUrl")  ?: "",
                            timestamp = doc.getLong("timestamp")   ?: 0L
                        )
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    error = e.localizedMessage ?: "Error al cargar pedidos"
                    isLoading = false
                }
        } else {
            error = "Usuario no autenticado"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Pedidos") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFE3F2FD))
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                orders.isEmpty() -> {
                    Text("No tienes pedidos aún", Modifier.align(Alignment.Center), fontSize = 16.sp)
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontSize = 16.sp)
                                    Text("€${item.price}", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}