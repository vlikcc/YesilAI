package com.yesilai.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.yesilai.app.R
import com.yesilai.app.data.repository.AuthRepository
import com.yesilai.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var genderMenuExpanded by remember { mutableStateOf(false) }
    
    val errorMessage by authRepository.errorMessage.collectAsState()
    val isLoading by authRepository.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YesilBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Logo
        Image(
            painter = painterResource(id = R.drawable.yesil_ai_koyu),
            contentDescription = "YesilAI Logo",
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = "YeşilAI'ya Kayıt Ol",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = YesilPrimary,
            textAlign = TextAlign.Center
        )
        
        // Subtitle
        Text(
            text = "Bağımlılıkla mücadele yolculuğunda sana destek olmak için buradayız.",
            fontSize = 16.sp,
            color = YesilTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        
        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = YesilError,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Form Fields
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Name Fields Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("İsim", color = YesilTextPlaceholder) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = YesilBorder,
                        focusedBorderColor = YesilPrimary,
                        unfocusedContainerColor = YesilBackground,
                        focusedContainerColor = YesilBackground,
                        focusedTextColor = YesilTextPrimary,
                        unfocusedTextColor = YesilTextPrimary
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Soyisim", color = YesilTextPlaceholder) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = YesilBorder,
                        focusedBorderColor = YesilPrimary,
                        unfocusedContainerColor = YesilBackground,
                        focusedContainerColor = YesilBackground,
                        focusedTextColor = YesilTextPrimary,
                        unfocusedTextColor = YesilTextPrimary
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )
            }
            
            // Age and Gender Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("Yaş", color = YesilTextPlaceholder) },
                    modifier = Modifier.width(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = YesilBorder,
                        focusedBorderColor = YesilPrimary,
                        unfocusedContainerColor = YesilBackground,
                        focusedContainerColor = YesilBackground,
                        focusedTextColor = YesilTextPrimary,
                        unfocusedTextColor = YesilTextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isLoading
                )
                
                // Gender Picker
                ExposedDropdownMenuBox(
                    expanded = genderMenuExpanded,
                    onExpandedChange = { genderMenuExpanded = !genderMenuExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedGender?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Cinsiyet", color = YesilTextPlaceholder) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select",
                                tint = YesilTextSecondary
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = YesilBorder,
                            focusedBorderColor = YesilPrimary,
                            unfocusedContainerColor = YesilBackground,
                            focusedContainerColor = YesilBackground,
                            focusedTextColor = YesilTextPrimary,
                            unfocusedTextColor = YesilTextPrimary
                        ),
                        enabled = !isLoading
                    )
                    
                    ExposedDropdownMenu(
                        expanded = genderMenuExpanded,
                        onDismissRequest = { genderMenuExpanded = false }
                    ) {
                        Gender.values().forEach { gender ->
                            DropdownMenuItem(
                                text = { Text(gender.displayName) },
                                onClick = {
                                    selectedGender = gender
                                    genderMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("E-posta Adresi", color = YesilTextPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = YesilBorder,
                    focusedBorderColor = YesilPrimary,
                    unfocusedContainerColor = YesilBackground,
                    focusedContainerColor = YesilBackground,
                    focusedTextColor = YesilTextPrimary,
                    unfocusedTextColor = YesilTextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Şifre", color = YesilTextPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = YesilBorder,
                    focusedBorderColor = YesilPrimary,
                    unfocusedContainerColor = YesilBackground,
                    focusedContainerColor = YesilBackground,
                    focusedTextColor = YesilTextPrimary,
                    unfocusedTextColor = YesilTextPrimary
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Şifre Tekrar", color = YesilTextPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = YesilBorder,
                    focusedBorderColor = YesilPrimary,
                    unfocusedContainerColor = YesilBackground,
                    focusedContainerColor = YesilBackground,
                    focusedTextColor = YesilTextPrimary,
                    unfocusedTextColor = YesilTextPrimary
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading
            )
        }
        
        // Register Button
        Button(
            onClick = {
                scope.launch {
                    if (authRepository.register(email, password, confirmPassword)) {
                        // Save additional user data to Firestore
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            val userData = hashMapOf<String, Any>(
                                "email" to email,
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                            
                            age.toIntOrNull()?.let { userData["age"] = it }
                            selectedGender?.let { userData["gender"] = it.rawValue }
                            
                            try {
                                db.collection("users").document(userId).set(userData).await()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        onNavigateToMain()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(25.dp)),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YesilPrimary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Kayıt Ol",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Login Link
        TextButton(
            onClick = onNavigateToLogin,
            enabled = !isLoading
        ) {
            Text(
                text = "Zaten hesabın var mı? Giriş yap",
                fontSize = 14.sp,
                color = YesilPrimary,
                textDecoration = TextDecoration.Underline
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}
