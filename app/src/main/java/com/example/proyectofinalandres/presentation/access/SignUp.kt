package com.example.proyectofinalandres.presentation.access

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinalandres.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUp(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navigateToLogin: () -> Unit,
    navigateToInicio: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var birthDate by remember { mutableStateOf("") }
    val dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy")
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }

    // Validamos que todos los campos estén completos y que las contraseñas coincidan
    val isFormValid = username.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            (password == confirmPassword) &&
            birthDate.isNotBlank()

    // Si showDatePicker = true, mostramos el DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        birthDate = date.format(dateFormatter)
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Flecha para regresar a “Inicio”
            Image(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Volver",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navigateToInicio() }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Registrarse",
                fontSize = 28.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo: nombre de usuario
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = attemptedSubmit && username.isBlank()
            )
            if (attemptedSubmit && username.isBlank()) {
                Text(
                    text = "El nombre de usuario es obligatorio",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: correo electrónico
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = attemptedSubmit && email.isBlank()
            )
            if (attemptedSubmit && email.isBlank()) {
                Text(
                    text = "El correo es obligatorio",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.hide_source
                                else R.drawable.red_eye
                            ),
                            contentDescription = if (passwordVisible)
                                "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = attemptedSubmit && password.isBlank()
            )
            if (attemptedSubmit && password.isBlank()) {
                Text(
                    text = "La contraseña es obligatoria",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: confirmar contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (confirmPasswordVisible) R.drawable.hide_source
                                else R.drawable.red_eye
                            ),
                            contentDescription = if (confirmPasswordVisible)
                                "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = attemptedSubmit && (confirmPassword.isBlank() || confirmPassword != password)
            )
            if (attemptedSubmit && confirmPassword.isBlank()) {
                Text(
                    text = "La confirmación es obligatoria",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            } else if (attemptedSubmit && confirmPassword != password) {
                Text(
                    text = "Las contraseñas no coinciden",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: fecha de nacimiento (solo lectura)
            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                label = { Text("Fecha de nacimiento *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.calendar_month),
                        contentDescription = "Seleccionar fecha",
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                },
                isError = attemptedSubmit && birthDate.isBlank()
            )
            if (attemptedSubmit && birthDate.isBlank()) {
                Text(
                    text = "La fecha de nacimiento es obligatoria",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón “Registrarse”
            Button(
                onClick = {
                    attemptedSubmit = true
                    if (isFormValid) {
                        errorMessage = null
                        isLoading = true


                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = auth.currentUser!!.uid


                                    val nuevoUsuario = mapOf(
                                        "name" to username,
                                        "username" to username,
                                        "email" to email,
                                        "description" to "",
                                        "image" to "",
                                        "products" to listOf<Any>(),
                                        "birthDate" to birthDate,
                                        "isAdmin" to false
                                    )

                                    db.collection("users")
                                        .document(uid)
                                        .set(nuevoUsuario)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            navigateToLogin()
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = e.localizedMessage
                                        }
                                } else {
                                    isLoading = false
                                    errorMessage = task.exception?.message
                                }
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color.Black else Color.LightGray,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Registrarse", fontSize = 16.sp)
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = msg,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}