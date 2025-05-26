package com.example.proyectofinalandres.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinalandres.presentation.modelo.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navigateToInicio: () -> Unit,  // cambiado el nombre
    navigateBack: () -> Unit
) {
    val uid = auth.currentUser?.uid
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    user = it.toObject(User::class.java)
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            if (user != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text("Correo: ${user!!.email}", fontSize = 18.sp)
                    Text("Nombre: ${user!!.name}", fontSize = 18.sp)
                    Text("Fecha de nacimiento: ${user!!.birthDate}", fontSize = 18.sp)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            auth.signOut()
                            navigateToInicio()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}