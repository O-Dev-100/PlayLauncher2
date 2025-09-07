package com.example.gamebooster.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gamebooster.R
import com.example.gamebooster.screens.LanguageDropdown
import com.example.gamebooster.ui.theme.Montserrat
import com.example.gamebooster.viewmodel.BoosterViewModel

@Composable
fun Sidebar(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val buttonColor = if (isDarkTheme) Color(0xFF4A4A4A) else Color(0xFFE0E0D1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header con branding y botón de cerrar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.navigate("dashboard") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Text(
                    text = "PlayLauncher",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = textColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            IconButton(onClick = { navController.navigate("support") }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_achievement),
                    contentDescription = stringResource(R.string.support),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Navegación principal con iconos
        val navItems = listOf(
            Triple(R.drawable.ic_home, R.string.dashboard, "dashboard"),
            Triple(R.drawable.ic_theme, R.string.ai_analysis, "ai_analysis"),
            Triple(R.drawable.ic_menu, R.string.gfx_tool, "gfx_tool"),
            Triple(R.drawable.ic_placeholder, R.string.quick_access, "quick_access"),
            Triple(R.drawable.ic_delete, R.string.boost_configs, "boost_configs"),
            Triple(R.drawable.ic_achievement, R.string.history, "history")
        )
        navItems.forEach { (icon, label, route) ->
            Button(
                onClick = { navController.navigate(route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(label),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )
            }
        }

        // Botón de login destacado
        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = stringResource(R.string.welcome_subtitle),
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.welcome_subtitle),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Selector de idioma y tema, más moderno
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.select_language),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            LanguageDropdown(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageChange,
                isDarkTheme = isDarkTheme
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(buttonColor)
            ) {
                Icon(
                    painter = painterResource(id = if (isDarkTheme) R.drawable.ic_sun else R.drawable.ic_moon),
                    contentDescription = stringResource(R.string.toggle_theme),
                    tint = textColor
                )
            }
        }
    }
}