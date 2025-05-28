package com.example.proyectofinalandres

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinalandres.ui.theme.ProyectoFinalAndresTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            ProyectoFinalAndresTheme {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationWrapper(navController, auth, db)

                    // Corrutina para iniciar sesión automático si el usuario ya se ha loggeado

                    LaunchedEffect(auth.currentUser) {
                        auth.currentUser?.let {
                            navController.navigate("home") {
                                popUpTo("inicio") { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}
