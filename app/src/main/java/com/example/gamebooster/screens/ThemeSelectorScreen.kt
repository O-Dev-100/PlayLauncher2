package com.example.gamebooster.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamebooster.ui.theme.PredefinedThemes
import com.example.gamebooster.viewmodel.AppSettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.border

@Composable
fun ThemeSelectorScreen(
    navController: NavController,
    appSettingsViewModel: AppSettingsViewModel = viewModel()
) {
    val selectedThemeIndex by appSettingsViewModel.customThemeIndex.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Elige tu tema",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        PredefinedThemes.forEachIndexed { index, theme ->
            if (index == 1) { // Suponiendo que el tema 1 es el dark
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            val themeName = when (theme.name.lowercase()) {
                "dark" -> "Oscuro"
                "light" -> "Claro"
                "high contrast", "contraste alto" -> "High Contrast"
                else -> theme.name
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable {
                        appSettingsViewModel.setCustomThemeIndex(index)
                        if (themeName == "High Contrast") {
                            appSettingsViewModel.setDarkTheme(true)
                            // Aquí puedes forzar el color amarillo en el ViewModel o tema
                        }
                        navController.popBackStack()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (themeName == "High Contrast") listOf(Color.Yellow, Color.Black) else listOf(theme.primary, theme.secondary)
                            )
                        )
                        .border(
                            width = if (selectedThemeIndex == index) 4.dp else 2.dp,
                            color = if (selectedThemeIndex == index) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = themeName,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
} 