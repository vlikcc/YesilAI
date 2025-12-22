package com.yesilai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yesilai.app.ui.theme.YesilBackground
import com.yesilai.app.ui.theme.YesilTextPrimary
import com.yesilai.app.ui.theme.YesilTextSecondary

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YesilBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚙️",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        
        Text(
            text = "Ayarlar",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = YesilTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Uygulama ayarları ve tercihleri\nyakında eklenecek",
            fontSize = 16.sp,
            color = YesilTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
