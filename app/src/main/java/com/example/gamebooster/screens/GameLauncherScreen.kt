package com.example.gamebooster.screens

import com.example.gamebooster.R
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gamebooster.data.remote.model.RawgGameDetails
import com.example.gamebooster.model.InstalledApp
import com.example.gamebooster.viewmodel.BoosterViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameLauncherScreen(
    navController: NavController,
    viewModel: BoosterViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ViewModel states
    val uiState by viewModel.uiState.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Filter games based on search and sort by favorites using derivedStateOf for better performance
    val filteredGames by remember {
        derivedStateOf {
            val games = if (searchQuery.isEmpty()) {
                uiState.installedGames.filter { it.isGame }
            } else {
                uiState.installedGames.filter {
                    it.isGame && it.appName.contains(searchQuery, ignoreCase = true)
                }
            }
            // Sort favorites first
            games.sortedWith(compareByDescending<InstalledApp> { it.packageName in uiState.favoriteGames })
        }
    }

    // Theme colors using Material 3 semantic colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        if (uiState.isLoading) {
            // Loading placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando juegos...",
                        color = textColor,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // TopAppBar with navigation and time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: PlayLauncher icon and text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.navigate("dashboard") }) {
                            Icon(
                                painter = painterResource(id = R.drawable.playlauncher),
                                contentDescription = "PlayLauncher",
                                tint = textColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "PlayLauncher",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    // Right side: Time, news, and power off
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Current time
                        var currentTime by remember { mutableStateOf("") }
                        LaunchedEffect(Unit) {
                            while (true) {
                                val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date())
                                currentTime = time
                                kotlinx.coroutines.delay(60000) // Update every minute
                            }
                        }
                        Text(
                            text = currentTime,
                            fontSize = 16.sp,
                            color = textColor,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        // News icon
                        IconButton(onClick = { navController.navigate("gaming_news") }) {
                            Icon(
                                imageVector = Icons.Default.Article,
                                contentDescription = "Gaming News",
                                tint = textColor
                            )
                        }

                        // Power off icon
                        var showPowerOffDialog by remember { mutableStateOf(false) }
                        IconButton(onClick = { showPowerOffDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.PowerOff,
                                contentDescription = "Power Off",
                                tint = Color.Red
                            )
                        }

                        // Power off confirmation dialog
                        if (showPowerOffDialog) {
                            AlertDialog(
                                onDismissRequest = { showPowerOffDialog = false },
                                title = { Text("Cerrar Aplicación") },
                                text = { Text("¿Estás seguro de que quieres cerrar la aplicación?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showPowerOffDialog = false
                                            // Close the app
                                            (context as? android.app.Activity)?.finish()
                                        }
                                    ) {
                                        Text("Sí")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showPowerOffDialog = false }) {
                                        Text("No")
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search games...", color = textColor.copy(alpha = 0.6f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = textColor)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textColor)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // System Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "System Status",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // System metrics in a grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SystemMetricCard(
                                label = "CPU",
                                value = "${uiState.ramUsagePercent.toInt()}%",
                                progress = uiState.ramUsagePercent / 100f,
                                color = Color(0xFF4CAF50),
                                textColor = textColor
                            )
                            SystemMetricCard(
                                label = "RAM",
                                value = "${String.format("%.0f", uiState.ramUsagePercent)}%",
                                progress = uiState.ramUsagePercent / 100f,
                                color = Color(0xFF2196F3),
                                textColor = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SystemMetricCard(
                                label = "Storage",
                                value = "${String.format("%.0f", uiState.storageUsagePercent)}%",
                                progress = uiState.storageUsagePercent / 100f,
                                color = Color(0xFFFF9800),
                                textColor = textColor
                            )
                            SystemMetricCard(
                                label = "Battery",
                                value = "${uiState.batteryLevel}%",
                                progress = uiState.batteryLevel / 100f,
                                color = if (uiState.batteryLevel < 20) Color.Red else Color(0xFF9C27B0),
                                textColor = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Games section header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Installed Games (${filteredGames.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    if (filteredGames.isNotEmpty()) {
                        TextButton(onClick = { /* Refresh games */ }) {
                            Text("Refresh", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Games list
                if (filteredGames.isEmpty()) {
                    // Empty state
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = textColor.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) "No games found" else "No games match your search",
                                fontSize = 16.sp,
                                color = textColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Install some games to see them here",
                                    fontSize = 14.sp,
                                    color = textColor.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(filteredGames) { game ->
                            GameCardNew(
                                game = game,
                                isSelected = uiState.selectedGame?.packageName == game.packageName,
                                isFavorite = game.packageName in uiState.favoriteGames,
                                onClick = { viewModel.onGameSelected(game) },
                                onToggleFavorite = { viewModel.toggleFavoriteGame(game.packageName) },
                                onLaunch = null,
                                isDarkTheme = isDarkTheme,
                                textColor = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = uiState.isGameDetailsVisible && uiState.selectedGame != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    uiState.selectedGame?.let { selected ->
                        GameDetailsCard(
                            game = selected,
                            fps = 0f, // We'll use a placeholder for now
                            rawg = uiState.rawgDetails,
                            isLoading = uiState.isLoadingGameDetails,
                            error = uiState.gameDetailsError,
                            isOptimizing = isOptimizing,
                            onBoost = { scope.launch { viewModel.performBoost() } },
                            onLaunch = { scope.launch { viewModel.launchApp(selected.packageName) } },
                            onClose = { viewModel.closeGameDetails() },
                            isDarkTheme = isDarkTheme,
                            textColor = textColor,
                            cardColor = cardColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SystemMetricCard(
    label: String,
    value: String,
    progress: Float,
    color: Color,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun GameCardNew(
    game: InstalledApp,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onLaunch: (() -> Unit)?,
    isDarkTheme: Boolean,
    textColor: Color
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val selectedColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedColor else cardColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row with favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color(0xFFFFD700) else textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Game icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (game.icon != null) {
                    // Note: In a real implementation, you'd use AsyncImage or similar
                    // to load the actual app icon
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = textColor.copy(alpha = 0.7f)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            // Game info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = game.appName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.height(32.dp)
                )

                Text(
                    text = game.category,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }

            // Actions moved to details panel
        }
    }
}

@Composable
fun GameDetailsCard(
    game: InstalledApp,
    fps: Float,
    rawg: RawgGameDetails?,
    isLoading: Boolean,
    error: String?,
    isOptimizing: Boolean,
    onBoost: () -> Unit,
    onLaunch: () -> Unit,
    onClose: () -> Unit,
    isDarkTheme: Boolean,
    textColor: Color,
    cardColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.appName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = rawg?.genres?.joinToString { it.name } ?: game.category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }

                Column {
                    Text(
                        text = "FPS",
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$fps",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            rawg?.description_raw?.let { desc ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = desc, color = textColor.copy(alpha = 0.9f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onLaunch,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Launch Game")
                }

                Button(
                    onClick = onBoost,
                    enabled = !isOptimizing,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (isOptimizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Boost")
                    }
                }
            }
        }
    }
}