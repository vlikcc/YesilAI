package com.yesilai.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.yesilai.app.R
import com.yesilai.app.data.repository.AuthRepository
import com.yesilai.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("E-posta Adresi", color = YesilTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = YesilBorder,
                    focusedBorderColor = YesilPrimary,
                    unfocusedContainerColor = YesilBackground,
                    focusedContainerColor = YesilBackground
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Şifre", color = YesilTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = YesilBorder,
                    focusedBorderColor = YesilPrimary,
                    unfocusedContainerColor = YesilBackground,
                    focusedContainerColor = YesilBackground
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Şifre Tekrar", color = YesilTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = YesilBorder,
                    focusedBorderColor = YesilPrimary,
                    unfocusedContainerColor = YesilBackground,
                    focusedContainerColor = YesilBackground
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
