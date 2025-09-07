package com.example.gamebooster.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamebooster.R
import com.example.gamebooster.data.GameDataService
import com.example.gamebooster.model.GameInfo
import com.example.gamebooster.ui.theme.Montserrat
import com.example.gamebooster.viewmodel.BoosterViewModel
import kotlinx.coroutines.launch

@Composable
fun GameListScreen(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    selectedLanguage: String
) {
    val context = LocalContext.current
    val gameDataService = remember { GameDataService(context) }
    val coroutineScope = rememberCoroutineScope()
    var installedGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var popularGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var favoriteGames by remember { mutableStateOf<Set<String>>(emptySet()) }

    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val primaryColor = if (isDarkTheme) Color(0xFF4CAF50) else Color(0xFF2E7D32)

    // Shimmer effect for loading
    val shimmerAlpha = rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                installedGames = gameDataService.getInstalledGames()
                popularGames = gameDataService.getPopularGames()
            } catch (e: Exception) {
                // Handle errors silently
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.game_list_title),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                coroutineScope.launch {
                    popularGames = if (it.isNotEmpty()) {
                        gameDataService.searchGames(it)
                    } else {
                        gameDataService.getPopularGames()
                    }
                }
            },
            placeholder = { Text("Buscar juegos...", color = textColor.copy(alpha = 0.6f)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = Montserrat,
                fontSize = 16.sp
            )
        )

        if (isLoading) {
            // Shimmer loading
            repeat(3) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .alpha(shimmerAlpha),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .height(18.dp)
                                    .fillMaxWidth(0.7f)
                                    .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .height(14.dp)
                                    .fillMaxWidth(0.5f)
                                    .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Favorites section
                if (favoriteGames.isNotEmpty()) {
                    item {
                        Text(
                            text = "Favoritos",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = primaryColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(installedGames.filter { favoriteGames.contains(it.packageName) }) { game ->
                        GameCardSimple(
                            game = game,
                            isFavorite = true,
                            onToggleFavorite = {
                                favoriteGames = if (favoriteGames.contains(game.packageName)) {
                                    favoriteGames - game.packageName
                                } else {
                                    favoriteGames + game.packageName
                                }
                            },
                            onLaunch = {
                                context.packageManager.getLaunchIntentForPackage(game.packageName)?.let {
                                    context.startActivity(it)
                                }
                            },
                            textColor = textColor,
                            cardColor = cardColor,
                            primaryColor = primaryColor
                        )
                    }
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                // Installed games
                if (installedGames.isNotEmpty()) {
                    item {
                        Text(
                            text = "Juegos Instalados (${installedGames.size})",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(installedGames.filter { !favoriteGames.contains(it.packageName) }) { game ->
                        GameCardSimple(
                            game = game,
                            isFavorite = false,
                            onToggleFavorite = {
                                favoriteGames = if (favoriteGames.contains(game.packageName)) {
                                    favoriteGames - game.packageName
                                } else {
                                    favoriteGames + game.packageName
                                }
                            },
                            onLaunch = {
                                context.packageManager.getLaunchIntentForPackage(game.packageName)?.let {
                                    context.startActivity(it)
                                }
                            },
                            textColor = textColor,
                            cardColor = cardColor,
                            primaryColor = primaryColor
                        )
                    }
                }

                // Recommended games
                if (popularGames.isNotEmpty()) {
                    item {
                        Text(
                            text = "Juegos Populares",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(popularGames) { game ->
                        GameCardRecommended(
                            game = game,
                            onInstall = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://play.google.com/store/apps/details?id=${game.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            textColor = textColor,
                            cardColor = cardColor,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCardSimple(
    game: GameInfo,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onLaunch: () -> Unit,
    textColor: Color,
    cardColor: Color,
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 4.dp)
            .clickable { onLaunch() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game icon
            AsyncImage(
                model = game.icon,
                contentDescription = game.appName,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                fallback = painterResource(id = R.drawable.ic_placeholder),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.appName,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor,
                    maxLines = 1
                )
                if (game.category.isNotEmpty()) {
                    Text(
                        text = game.category,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
                if (game.size.isNotEmpty()) {
                    Text(
                        text = "Size: ${'$'}{game.size}",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                    tint = if (isFavorite) primaryColor else textColor.copy(alpha = 0.4f)
                )
            }
            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
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
fun GameCardRecommended(
    game: GameInfo,
    onInstall: () -> Unit,
    textColor: Color,
    cardColor: Color,
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game icon
            val localIcon = com.example.gamebooster.utils.GameIconUtils.getLocalGameIcon(game.packageName)
            if (localIcon != null && !game.isInstalled) {
                Icon(
                    painter = painterResource(id = localIcon),
                    contentDescription = game.appName,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                AsyncImage(
                    model = game.icon,
                    contentDescription = game.appName,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    fallback = painterResource(id = R.drawable.ic_placeholder),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.appName,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor,
                    maxLines = 1
                )
                // Keep for installed; for recommended we already show dev/price/size below
                if (game.size.isNotEmpty()) {
                    Text(
                        text = "Tamaño: ${game.size}",
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
            }
            Column(horizontalAlignment = Alignment.End) {
                if (game.developer.isNotEmpty()) {
                    Text(
                        text = game.developer,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                if (game.price.isNotEmpty()) {
                    Text(
                        text = game.price,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onInstall,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = stringResource(R.string.install_game),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}