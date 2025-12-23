package com.yesilai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.yesilai.app.R
import com.yesilai.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class Gender(val displayName: String, val rawValue: String) {
    MALE("Erkek", "erkek"),
    FEMALE("Kadın", "kadın"),
    PREFER_NOT_TO_SAY("Belirtmek İstemiyorum", "belirtmek_istemiyorum")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCompletionScreen(
    onNavigateToMain: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
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
        AsyncImage(
            model = R.drawable.yesil_ai_koyu,
            contentDescription = "YesilAI Logo",
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = "Profilini Tamamla",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = YesilPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = "Seni daha iyi tanımamız için birkaç bilgiye ihtiyacımız var.",
            fontSize = 16.sp,
            color = YesilTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
        
        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            placeholder = { Text("İsim", color = YesilTextPlaceholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YesilPrimary,
                unfocusedBorderColor = YesilBorder,
                focusedTextColor = YesilTextPrimary,
                unfocusedTextColor = YesilTextPrimary
            ),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Last Name
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            placeholder = { Text("Soyisim", color = YesilTextPlaceholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YesilPrimary,
                unfocusedBorderColor = YesilBorder,
                focusedTextColor = YesilTextPrimary,
                unfocusedTextColor = YesilTextPrimary
            ),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Age
        OutlinedTextField(
            value = age,
            onValueChange = { age = it.filter { c -> c.isDigit() } },
            placeholder = { Text("Yaş", color = YesilTextPlaceholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YesilPrimary,
                unfocusedBorderColor = YesilBorder,
                focusedTextColor = YesilTextPrimary,
                unfocusedTextColor = YesilTextPrimary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gender Selection
        Text(
            text = "Cinsiyet",
            fontSize = 14.sp,
            color = YesilTextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        // Gender Grid - 3 options in row layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GenderButton(
                gender = Gender.MALE,
                isSelected = selectedGender == Gender.MALE,
                onClick = { selectedGender = Gender.MALE },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            )
            GenderButton(
                gender = Gender.FEMALE,
                isSelected = selectedGender == Gender.FEMALE,
                onClick = { selectedGender = Gender.FEMALE },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        GenderButton(
            gender = Gender.PREFER_NOT_TO_SAY,
            isSelected = selectedGender == Gender.PREFER_NOT_TO_SAY,
            onClick = { selectedGender = Gender.PREFER_NOT_TO_SAY },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Save Button
        Button(
            onClick = {
                scope.launch {
                    if (firstName.isBlank()) {
                        errorMessage = "İsim gereklidir"
                        return@launch
                    }
                    if (lastName.isBlank()) {
                        errorMessage = "Soyisim gereklidir"
                        return@launch
                    }
                    
                    isLoading = true
                    errorMessage = ""
                    
                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        errorMessage = "Kullanıcı bulunamadı"
                        isLoading = false
                        return@launch
                    }
                    
                    val userData = hashMapOf<String, Any>(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    
                    age.toIntOrNull()?.let { userData["age"] = it }
                    selectedGender?.let { userData["gender"] = it.rawValue }
                    
                    try {
                        db.collection("users").document(userId)
                            .set(userData, com.google.firebase.firestore.SetOptions.merge())
                            .await()
                        onNavigateToMain()
                    } catch (e: Exception) {
                        errorMessage = "Kayıt hatası: ${e.localizedMessage}"
                    }
                    
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YesilPrimary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Devam Et",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skip Button
        TextButton(
            onClick = onNavigateToMain,
            enabled = !isLoading
        ) {
            Text(
                text = "Daha sonra tamamla",
                fontSize = 14.sp,
                color = YesilTextSecondary
            )
        }
    }
}

@Composable
private fun GenderButton(
    gender: Gender,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) YesilPrimary else Color.White)
            .border(
                width = 1.dp,
                color = if (isSelected) YesilPrimary else YesilBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = gender.displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else YesilTextPrimary
        )
    }
}
