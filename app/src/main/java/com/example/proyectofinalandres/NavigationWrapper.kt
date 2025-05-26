package com.example.proyectofinalandres

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyectofinalandres.presentation.access.Login
import com.example.proyectofinalandres.presentation.access.SignUp
import com.example.proyectofinalandres.presentation.home.HomeScreen
import com.example.proyectofinalandres.presentation.home.SearchScreen
import com.example.proyectofinalandres.presentation.inicio.Inicio
import com.example.proyectofinalandres.presentation.modelo.Product
import com.example.proyectofinalandres.presentation.product.ProductDetailScreen
import com.example.proyectofinalandres.presentation.user.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth,
    db: FirebaseFirestore
) {
    NavHost(navController = navHostController, startDestination = "inicio") {

        composable("inicio") {
            Inicio(
                navigateToLogin = {
                    navHostController.navigate("login") {
                        popUpTo("inicio")
                    }
                },
                navigateToSignUp = {
                    navHostController.navigate("signup") {
                        popUpTo("inicio")
                    }
                }
            )
        }

        composable("signup") {
            SignUp(
                auth = auth,
                db = db,
                navigateToLogin = {
                    navHostController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                navigateToInicio = {
                    navHostController.navigate("inicio") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            Login(
                auth = auth,
                navigateBack = {
                    navHostController.popBackStack()
                },
                navigateToHome = {
                    navHostController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                auth = auth,
                db = db,
                onHomeClick = { /* Ya en home */ },
                onSearchClick = {
                    navHostController.navigate("search")
                },
                onCartClick = { /* Por implementar */ },
                onAddClick = { /* Por implementar */ },
                onProductClick = { product ->
                    navHostController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("product", product)
                    navHostController.navigate("productDetail")
                },
                onProfileClick = {
                    navHostController.navigate("profile")
                }
            )
        }

        composable("search") {
            SearchScreen(
                db = db,
                onBack = { navHostController.popBackStack() },
                onProductClick = { productId ->
                    navHostController.navigate("productDetail/$productId")
                }
            )
        }

        composable("productDetail") {
            val product = navHostController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Product>("product")
            product?.let {
                ProductDetailScreen(
                    product = it,
                    auth = auth,
                    db = db,
                    onAddedToCart = { navHostController.popBackStack() }
                )
            }
        }

        composable("profile") {
            ProfileScreen(
                auth = auth,
                db = db,
                navigateToInicio = {
                    navHostController.navigate("inicio") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                navigateBack = {
                    navHostController.popBackStack()
                }
            )
        }

    }
}