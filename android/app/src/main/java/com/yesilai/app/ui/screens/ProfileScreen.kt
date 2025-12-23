package com.yesilai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yesilai.app.data.repository.AuthRepository
import com.yesilai.app.ui.theme.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: (() -> Unit)? = null
) {
    val authRepository = remember { AuthRepository() }
    val currentUserEmail by authRepository.currentUserEmail.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var userData by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val email = auth.currentUser?.email ?: currentUserEmail
    
    // Load user data
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                val document = db.collection("users").document(userId).get().await()
                if (document.exists()) {
                    userData = document.data ?: emptyMap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isLoading = false
    }
    
    val fullName = remember(userData) {
        val firstName = userData["firstName"] as? String ?: ""
        val lastName = userData["lastName"] as? String ?: ""
        val name = "$firstName $lastName".trim()
        name.ifEmpty { email }
    }
    
    val formattedDate = remember(userData, auth.currentUser) {
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))
        
        // Try Firestore Timestamp first
        val timestamp = userData["createdAt"] as? Timestamp
        if (timestamp != null) {
            formatter.format(timestamp.toDate())
        } else {
            // Fallback to Firebase Auth creation date
            val creationTimestamp = auth.currentUser?.metadata?.creationTimestamp
            if (creationTimestamp != null && creationTimestamp > 0) {
                formatter.format(Date(creationTimestamp))
            } else {
                "Bilinmiyor"
            }
        }
    }
    
    val genderDisplay = remember(userData) {
        when (userData["gender"] as? String) {
            "erkek" -> "Erkek"
            "kadın" -> "Kadın"
            "belirtmek_istemiyorum" -> "Belirtmek İstemiyorum"
            else -> null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil",
                        fontWeight = FontWeight.Bold,
                        color = YesilTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YesilBackground,
                    titleContentColor = YesilTextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(YesilBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(YesilPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(48.dp),
                    tint = YesilPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Name
            Text(
                text = fullName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = YesilTextPrimary
            )
            
            // User Email
            Text(
                text = email,
                fontSize = 14.sp,
                color = YesilTextSecondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isLoading) {
                CircularProgressIndicator(color = YesilPrimary)
            } else {
                // Profile Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = YesilSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        ProfileMenuItem(
                            icon = Icons.Default.Person,
                            title = "Ad Soyad",
                            value = fullName
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = YesilBorder
                        )
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Email,
                            title = "E-posta",
                            value = email
                        )
                        
                        val age = userData["age"] as? Long
                        if (age != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                color = YesilBorder
                            )
                            
                            ProfileMenuItem(
                                icon = Icons.Default.Numbers,
                                title = "Yaş",
                                value = age.toString()
                            )
                        }
                        
                        if (genderDisplay != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                color = YesilBorder
                            )
                            
                            ProfileMenuItem(
                                icon = Icons.Default.People,
                                title = "Cinsiyet",
                                value = genderDisplay
                            )
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = YesilBorder
                        )
                        
                        ProfileMenuItem(
                            icon = Icons.Default.CalendarMonth,
                            title = "Kayıt Tarihi",
                            value = formattedDate
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Logout Button Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = YesilSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(24.dp),
                            tint = YesilError
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Çıkış Yap",
                            fontSize = 16.sp,
                            color = YesilError
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Çıkış Yap") },
            text = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authRepository.logout()
                        onLogout?.invoke()
                    }
                ) {
                    Text("Çıkış Yap", color = YesilError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = YesilPrimary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                color = YesilTextSecondary
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = YesilTextPrimary
            )
        }
    }
}
