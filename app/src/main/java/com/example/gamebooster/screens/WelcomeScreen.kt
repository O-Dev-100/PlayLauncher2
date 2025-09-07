package com.example.gamebooster.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gamebooster.R
import com.example.gamebooster.ui.theme.GameBoosterTheme
import com.example.gamebooster.ui.theme.Montserrat
import java.util.Locale
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    var localeUpdated by remember { mutableStateOf(0) }
    val defaultLanguage = remember { mutableStateOf("English") }

    LaunchedEffect(selectedLanguage) {
        setLocale(context, selectedLanguage)
        localeUpdated++
    }

    val backgroundGradient = if (isDarkTheme) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1A1A1A), Color(0xFF4A4A4A)),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFF5F5E6), Color(0xFFE0E0D1)),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )
    }
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val buttonContainerColor = if (isDarkTheme) Color(0xFF4A4A4A) else Color(0xFFE0E0D1)
    val buttonContentColor = if (isDarkTheme) Color.White else Color.Black

    GameBoosterTheme(darkTheme = isDarkTheme) {
        Box(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Fila de toggles arriba del todo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onThemeToggle,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(buttonContainerColor)
                    ) {
                        Icon(
                            painter = painterResource(id = if (isDarkTheme) R.drawable.ic_sun else R.drawable.ic_moon),
                            contentDescription = stringResource(R.string.toggle_theme),
                            tint = buttonContentColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { navController.navigate("theme_selector") },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(buttonContainerColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Seleccionar tema",
                            tint = buttonContentColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LanguageDropdown(
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = onLanguageChange,
                        isDarkTheme = isDarkTheme
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.playlauncher),
                    contentDescription = stringResource(R.string.welcome_title),
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5E6))
                        .shadow(16.dp, CircleShape)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.welcome_title),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.welcome_subtitle),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonContainerColor,
                        contentColor = buttonContentColor
                    )
                ) {
                    Text(
                        text = stringResource(R.string.get_started),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageDropdown(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    isDarkTheme: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("English", "Spanish")
    val dropdownBackground = if (isDarkTheme) Color(0xFF4A4A4A) else Color(0xFFE0E0D1)
    val dropdownTextColor = if (isDarkTheme) Color.White else Color.Black

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = dropdownBackground,
                contentColor = dropdownTextColor
            ),
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Text(
                text = selectedLanguage,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_more),
                contentDescription = "Dropdown",
                tint = dropdownTextColor
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(dropdownBackground)
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = language,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Normal,
                            color = dropdownTextColor
                        )
                    },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}

