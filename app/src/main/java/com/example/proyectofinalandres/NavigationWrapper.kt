package com.example.proyectofinalandres

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyectofinalandres.presentation.access.Login
import com.example.proyectofinalandres.presentation.access.SignUp
import com.example.proyectofinalandres.presentation.home.HomeScreen
import com.example.proyectofinalandres.presentation.home.SearchScreen
import com.example.proyectofinalandres.presentation.inicio.Inicio
import com.example.proyectofinalandres.presentation.product.AddProductScreen
import com.example.proyectofinalandres.presentation.product.CartScreen
import com.example.proyectofinalandres.presentation.product.MyOrdersScreen
import com.example.proyectofinalandres.presentation.product.ProductDetailScreen
import com.example.proyectofinalandres.presentation.user.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationWrapper(
    navController: NavHostController,
    auth: FirebaseAuth,
    db: FirebaseFirestore
) {
    NavHost(navController = navController, startDestination = "inicio") {
        composable("inicio") {
            Inicio(
                navigateToLogin  = { navController.navigate("login") },
                navigateToSignUp = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignUp(
                auth = auth,
                db = db,
                navigateToLogin  = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                navigateToInicio = {
                    navController.navigate("inicio") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            Login(
                auth = auth,
                navigateBack   = { navController.popBackStack() },
                navigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                auth           = auth,
                db             = db,
                onHomeClick    = { /*…*/ },
                onSearchClick  = { navController.navigate("search") },
                onCartClick    = { navController.navigate("cart") },
                onAddClick     = { navController.navigate("addProduct") },
                onProductClick = { id -> navController.navigate("productDetail/$id") },
                onProfileClick = { navController.navigate("profile") }
            )
        }
        composable("search") {
            SearchScreen(
                db = db,
                onBack = { navController.popBackStack() },
                onProductClick = { /*…*/ }
            )
        }
        composable("cart") {
            CartScreen(
                auth             = auth,
                db               = db,
                navigateBack     = { navController.popBackStack() },
                navigateToOrders = { navController.navigate("orders") }
            )
        }
        composable("orders") {
            MyOrdersScreen(
                auth         = auth,
                db           = db,
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "productDetail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { back ->
            val pid = back.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(
                productId    = pid,
                auth         = auth,
                db           = db,
                navigateBack = { navController.popBackStack() },
                onAddedToCart= { navController.popBackStack() }
            )
        }
        // Nueva ruta para añadir producto
        composable("addProduct") {
            AddProductScreen(
                auth        = auth,
                db          = db,
                navigateBack= { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                auth,
                db,
                navigateToInicio = {
                    navController.navigate("inicio") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                navigateBack     = { navController.popBackStack() },
                navigateToOrders = { navController.navigate("orders") }
            )
        }
    }
}
