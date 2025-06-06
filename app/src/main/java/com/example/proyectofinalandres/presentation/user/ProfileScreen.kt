package com.example.proyectofinalandres.presentation.user

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectofinalandres.R
import com.example.proyectofinalandres.network.CloudinaryClient
import com.example.proyectofinalandres.presentation.modelo.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navigateToInicio: () -> Unit,
    navigateBack: () -> Unit,
    navigateToOrders: () -> Unit
) {
    val activity = (LocalActivity.current as? Activity)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val uid = auth.currentUser?.uid
    var user by remember { mutableStateOf<User?>(null) }

    var editableName by remember { mutableStateOf(false) }
    var editableEmail by remember { mutableStateOf(false) }
    var editableBirthDate by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    var imageUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val pickImage = rememberLauncherForActivityResult(contract = GetContent()) {
        imageUri = it
        if (it != null) {
            scope.launch {
                imageUploading = true
                try {
                    val input = context.contentResolver.openInputStream(it)!!
                    val tmp = File(context.cacheDir, "profile_upload.jpg")
                    FileOutputStream(tmp).use { out -> input.copyTo(out) }

                    val uploadedUrl = withContext(Dispatchers.IO) {
                        CloudinaryClient.uploadImage(tmp)
                    }

                    db.collection("users").document(uid!!).update("image", uploadedUrl)
                        .addOnSuccessListener {
                            user = user?.copy(image = uploadedUrl)
                        }

                } catch (e: Exception) {
                    errorMessage = "Error al subir imagen"
                } finally {
                    imageUploading = false
                }
            }
        }
    }

    val hasChanges = user?.let {
        name != it.name || email != it.email || birthDate != it.birthDate
    } ?: false

    LaunchedEffect(uid) {
        uid?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { snap ->
                    val fetchedUser = snap.toObject(User::class.java)
                    user = fetchedUser
                    name = fetchedUser?.name.orEmpty()
                    email = fetchedUser?.email.orEmpty()
                    birthDate = fetchedUser?.birthDate.orEmpty()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.four_lines),
                            contentDescription = "Más"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mis pedidos") },
                            onClick = {
                                menuExpanded = false
                                navigateToOrders()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                menuExpanded = false
                                auth.signOut()
                                navigateToInicio()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Salir") },
                            onClick = {
                                menuExpanded = false
                                activity?.finishAffinity()
                            }
                        )
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
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Fondo con imagen local con blur y transparencia
            Image(
                painter = painterResource(id = R.drawable.nvrmnd_fondo),
                contentDescription = "Fondo perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(2.dp)
                    .graphicsLayer { alpha = 0.3f }
            )

            user?.let {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp, start = 24.dp, end = 24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = it.image.ifEmpty {
                                "https://cdn.pixabay.com/photo/2023/02/18/11/00/icon-7797704_640.png"
                            },
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable { pickImage.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                        if (imageUploading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    Text(text = it.name, fontSize = 22.sp, color = Color.Black)

                    Divider(modifier = Modifier.fillMaxWidth(0.8f), color = Color.Gray)

                    EditableField("Correo", email, editableEmail, { email = it }) {
                        editableEmail = !editableEmail
                    }

                    EditableField("Nombre", name, editableName, { name = it }) {
                        editableName = !editableName
                    }

                    EditableField("Fecha de nacimiento", birthDate, editableBirthDate, { birthDate = it }) {
                        editableBirthDate = !editableBirthDate
                    }

                    if (hasChanges) {
                        Button(
                            onClick = {
                                val updatedUser = it.copy(
                                    name = name,
                                    email = email,
                                    birthDate = birthDate
                                )
                                db.collection("users").document(uid!!).set(updatedUser)
                                    .addOnSuccessListener {
                                        user = updatedUser
                                        editableEmail = false
                                        editableName = false
                                        editableBirthDate = false
                                    }
                            },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text("Guardar cambios")
                        }
                    }

                    errorMessage?.let {
                        Text(it, color = Color.Red)
                    }
                }
            } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    editable: Boolean,
    onValueChange: (String) -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.9f), shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
            if (editable) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(text = value, fontSize = 16.sp, color = Color.Black)
            }
        }
        IconButton(onClick = onEditClick) {
            Icon(
                painter = painterResource(id = R.drawable.edit),
                contentDescription = "Editar"
            )
        }
    }
}
