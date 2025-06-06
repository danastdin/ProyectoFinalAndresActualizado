package com.example.proyectofinalandres.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.proyectofinalandres.R
import com.example.proyectofinalandres.presentation.modelo.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    db: FirebaseFirestore,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit
) {
    var allProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }

    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var brandsMenuExpanded by remember { mutableStateOf(false) }
    val brands = listOf(
        "BALENCIAGA", "VETEMENTS", "Rick Owens",
        "Maison Margiela", "424", "HOOD BY AIR", "BAPE",
        "Acne Studios", "ERD", "Chrome Hearts",
        "Off-White", "Dior", "Gucci", "Prada",
        "Louis Vuitton", "Saint Laurent", "Givenchy",
        "A Bathing Ape", "Vetements", "Fear of God",
        "Neil Barrett", "Palm Angels", "Amiri"
    )

    LaunchedEffect(Unit) {
        val snapshot = db.collection("products").get().await()
        allProducts = snapshot.documents.mapNotNull {
            it.toObject(Product::class.java)?.copy(id = it.id)
        }
        loading = false
    }

    val filtered = remember(allProducts, query, selectedBrand) {
        allProducts.filter { product ->
            val matchesQuery = query.isBlank()
                    || product.name.contains(query, ignoreCase = true)
                    || product.brand.contains(query, ignoreCase = true)
            val matchesBrand = selectedBrand == null
                    || product.brand.equals(selectedBrand, ignoreCase = true)
            matchesQuery && matchesBrand
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar productos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { brandsMenuExpanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filtrar por marca"
                        )
                    }
                    DropdownMenu(
                        expanded = brandsMenuExpanded,
                        onDismissRequest = { brandsMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas") },
                            onClick = {
                                selectedBrand = null
                                brandsMenuExpanded = false
                            }
                        )
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand) },
                                onClick = {
                                    selectedBrand = brand
                                    brandsMenuExpanded = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
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
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(2.dp)
                    .graphicsLayer { alpha = 0.3f }
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar por nombre o marca") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search)
                )

                if (loading) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filtered.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No se encontraron productos", fontSize = 16.sp, color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered) { product ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onProductClick(product.id) },
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = product.imageUrl),
                                        contentDescription = product.name,
                                        modifier = Modifier
                                            .height(120.dp)
                                            .fillMaxWidth(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(product.brand, color = Color.Gray, fontSize = 12.sp)
                                    Text("${product.price} €", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
