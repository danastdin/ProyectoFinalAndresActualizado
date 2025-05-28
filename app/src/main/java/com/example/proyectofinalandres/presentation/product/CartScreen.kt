package com.example.proyectofinalandres.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
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

data class CartItem(
    val docId: String,
    val productId: String,
    val name: String,
    val price: Double,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navigateBack: () -> Unit,
    navigateToOrders: () -> Unit
) {
    val uid = auth.currentUser?.uid
    var items by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    // ids seleccionados
    val selected = remember { mutableStateListOf<String>() }

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener { snaps ->
                    items = snaps.documents.map { doc ->
                        CartItem(
                            docId    = doc.id,
                            productId= doc.getString("productId") ?: "",
                            name     = doc.getString("name")      ?: "",
                            price    = doc.getDouble("price")     ?: 0.0,
                            imageUrl = doc.getString("imageUrl")  ?: ""
                        )
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    error = e.localizedMessage ?: "Error al cargar el carrito"
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
                title = { Text("Mi Carrito") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFE3F2FD))
        ) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(error!!, color = Color.Red)
                    }
                }
                items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tu carrito está vacío", fontSize = 16.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items) { item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .toggleable(
                                        value = selected.contains(item.docId),
                                        onValueChange = { checked ->
                                            if (checked) selected.add(item.docId)
                                            else selected.remove(item.docId)
                                        }
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selected.contains(item.docId),
                                    onCheckedChange = null // lo maneja el Row.toggleable
                                )
                                Spacer(Modifier.width(8.dp))
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

                    // Botón de "Pagar"
                    Button(
                        onClick = {
                            // mover seleccionados a "orders" y borrarlos del carrito
                            uid?.let { u ->
                                val batch = db.batch()
                                val orderCol = db.collection("users").document(u).collection("orders")
                                val cartCol  = db.collection("users").document(u).collection("cart")
                                items.filter { it.docId in selected }.forEach { it ->
                                    val newOrder = orderCol.document()
                                    batch.set(newOrder, mapOf(
                                        "productId" to it.productId,
                                        "name"      to it.name,
                                        "price"     to it.price,
                                        "imageUrl"  to it.imageUrl,
                                        "timestamp" to System.currentTimeMillis()
                                    ))
                                    batch.delete(cartCol.document(it.docId))
                                }
                                batch.commit()
                                    .addOnSuccessListener {
                                        selected.clear()
                                        navigateToOrders()
                                    }
                            }
                        },
                        enabled = selected.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected.isNotEmpty()) Color.Black else Color.LightGray,
                            contentColor   = Color.White
                        )
                    ) {
                        Text("Pagar")
                    }
                }
            }
        }
    }
}