package com.example.gamebooster.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gamebooster.ui.theme.Montserrat
import com.example.gamebooster.R
import androidx.compose.ui.res.painterResource

@Composable
fun FeedbackScreen(
    navController: NavController,
    isDarkTheme: Boolean
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    var rating by remember { mutableStateOf(0) }
    var opinion by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color.Black else Color(0xFFF5F5E6))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header con flecha atrás
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = stringResource(R.string.back),
                    tint = textColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.anonymous_feedback),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { rating = i }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = opinion,
            onValueChange = { opinion = it },
            label = { Text(stringResource(R.string.feedback_hint), color = textColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = Montserrat,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (rating > 0 && opinion.isNotBlank()) {
                    saveFeedback(context, rating, opinion)
                    showSuccess = true
                    opinion = ""
                    rating = 0
                } else {
                    Toast.makeText(context, context.getString(R.string.feedback_hint), Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(stringResource(R.string.send_feedback), fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color.White)
        }
        if (showSuccess) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.feedback_thanks),
                color = Color(0xFF4CAF50),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun saveFeedback(context: Context, rating: Int, opinion: String) {
    val prefs = context.getSharedPreferences("feedback", Context.MODE_PRIVATE)
    prefs.edit().putInt("rating", rating).putString("opinion", opinion).apply()
} 