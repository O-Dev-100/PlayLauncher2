package com.example.gamebooster.screens

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamebooster.R
import com.example.gamebooster.ui.theme.Montserrat
import com.example.gamebooster.viewmodel.BoosterViewModel
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gamebooster.data.GooglePlayService
import com.example.gamebooster.model.GameInfo
import kotlinx.coroutines.launch
import com.example.gamebooster.utils.GameIconUtils

@Composable
fun QuickAccessScreen(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    selectedLanguage: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White

    // Estados para los juegos
    var installedGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var recommendedGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar juegos al iniciar
    LaunchedEffect(Unit) {
        val googlePlayService = GooglePlayService(context)
        installedGames = googlePlayService.getInstalledGames()
        recommendedGames = googlePlayService.getPopularGames()
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = stringResource(R.string.back),
                    tint = textColor
                )
            }
            Text(
                text = stringResource(R.string.quick_access),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Juegos instalados
                if (installedGames.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.installed_games),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(installedGames) { game ->
                        InstalledGameCard(
                            game = game,
                            onLaunch = {
                                try {
                                    val intent = context.packageManager.getLaunchIntentForPackage(game.packageName)
                                    if (intent != null) {
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    // Manejar error
                                }
                            },
                            isDarkTheme = isDarkTheme,
                            textColor = textColor,
                            cardColor = cardColor
                        )
                    }
                }

                // Juegos recomendados
                if (recommendedGames.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.recommended_games),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(recommendedGames) { game ->
                        RecommendedGameCard(
                            game = game,
                            onInstall = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("market://details?id=${game.packageName}")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback a Play Store web
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://play.google.com/store/apps/details?id=${game.packageName}")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            onWatchTrailer = { trailerUrl ->
                                if (trailerUrl != null) {
                                    // Abrir trailer en WebView o navegador
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl))
                                    context.startActivity(intent)
                                }
                            },
                            isDarkTheme = isDarkTheme,
                            textColor = textColor,
                            cardColor = cardColor
                        )
                    }
                }

                // Mensaje si no hay juegos
                if (installedGames.isEmpty() && recommendedGames.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Games,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = textColor.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.no_games_found),
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Instala algunos juegos para verlos aquí",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = textColor.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstalledGameCard(
    game: GameInfo,
    onLaunch: () -> Unit,
    isDarkTheme: Boolean,
    textColor: Color,
    cardColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLaunch() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del juego
            game.icon?.let { icon ->
                Icon(
                    imageVector = Icons.Default.Games,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } ?: run {
                Icon(
                    imageVector = Icons.Default.Games,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del juego
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.appName,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )
                if (game.category.isNotEmpty()) {
                    Text(
                        text = game.category,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                if (game.size.isNotEmpty()) {
                    Text(
                        text = game.size,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Botón de lanzar
            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.launch),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun RecommendedGameCard(
    game: GameInfo,
    onInstall: () -> Unit,
    onWatchTrailer: (String?) -> Unit,
    isDarkTheme: Boolean,
    textColor: Color,
    cardColor: Color
) {
    val localIcon = GameIconUtils.getLocalGameIcon(game.packageName)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono del juego
                when {
                    localIcon != null -> {
                        Icon(
                            painter = painterResource(id = localIcon),
                            contentDescription = game.appName,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                            tint = Color.Unspecified
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Games,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Información del juego
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.appName,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                    if (game.category.isNotEmpty()) {
                        Text(
                            text = game.category,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                    if (game.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Text(
                                text = game.rating.toString(),
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                    if (game.downloads.isNotEmpty()) {
                        Text(
                            text = game.downloads,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            if (game.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = game.description,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onInstall,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.install_game),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                
                if (game.trailerUrl != null) {
                    OutlinedButton(
                        onClick = { onWatchTrailer(game.trailerUrl) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Trailer",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// Clase de datos para juegos recomendados (mantener compatibilidad)
data class RecommendedGame(
    val name: String,
    val packageName: String,
    val playStoreId: String,
    val description: String,
    val category: String
) 