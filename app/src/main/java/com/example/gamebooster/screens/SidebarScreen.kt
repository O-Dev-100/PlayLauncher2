package com.example.gamebooster.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gamebooster.R
import com.example.gamebooster.ui.theme.Montserrat
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.ui.res.painterResource

@Composable
fun SidebarScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    selectedLanguage: String,
    onThemeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor
            )
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = stringResource(R.string.back),
                    tint = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.toggle_theme),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = textColor
            )
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onThemeChange(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Green,
                    checkedTrackColor = Color.Green.copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.select_language),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = textColor
            )
            Box {
                var expanded by remember { mutableStateOf(false) }
                Button(onClick = { expanded = true }) {
                    Text(
                        text = selectedLanguage,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf(stringResource(R.string.english), stringResource(R.string.spanish)).forEach { lang ->
                        DropdownMenuItem(onClick = {
                            onLanguageChange(lang)
                            expanded = false
                        }, text = {
                            Text(
                                text = lang,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = textColor
                            )
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("history") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.history), fontFamily = Montserrat, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { navController.navigate("gaming_news") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.gaming_news), fontFamily = Montserrat, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Playlauncher: The best free Launcher and Booster for Android is available right now!")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                navController.context.startActivity(shareIntent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.recommend_to_friend), fontFamily = Montserrat, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { navController.navigate("privacy_policy") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.privacy_policy), fontFamily = Montserrat, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { navController.navigate("notifications") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.notifications), fontFamily = Montserrat, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { navController.navigate("feedback") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.feedback), fontFamily = Montserrat, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { navController.navigate("theme_selector") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Palette, contentDescription = stringResource(R.string.select_theme), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.change_theme), fontFamily = Montserrat, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { /* Acción para cerrar la app */ android.os.Process.killProcess(android.os.Process.myPid()) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(stringResource(R.string.exit), fontFamily = Montserrat, color = Color.White)
        }
    }
}