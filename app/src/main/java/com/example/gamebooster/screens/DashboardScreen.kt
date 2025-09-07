package com.example.gamebooster.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.gamebooster.R
import com.example.gamebooster.data.GameDataService
import com.example.gamebooster.model.GameInfo
import com.example.gamebooster.ui.theme.Montserrat
import com.example.gamebooster.utils.GameIconUtils
import com.example.gamebooster.viewmodel.BoosterViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector

@SuppressLint("StringFormatInvalid")
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    selectedLanguage: String
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var isBoosting by remember { mutableStateOf(false) }
    var buttonScale by remember { mutableStateOf(1f) }
    val ramUsage by viewModel.ramUsage.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val storageUsage by viewModel.storageUsage.collectAsState()
    val fps by viewModel.fps.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val boostHistory by viewModel.boostHistory.collectAsState()
    val safeBoost by viewModel.safeBoost.collectAsState()
    val showFeedbackDetails = remember { mutableStateOf(false) }

    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White

    var recommendedGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    LaunchedEffect(Unit) {
        recommendedGames = GameDataService(context).getPopularGames()
    }

    // Calcular tiempo desde el último boost
    val lastBoostTimestamp = boostHistory.firstOrNull()?.timestamp
    val lastBoostText = lastBoostTimestamp?.let {
        val now = System.currentTimeMillis()
        val diffMillis = now - it
        val diffHours = diffMillis / (1000 * 60 * 60)
        val diffMinutes = (diffMillis / (1000 * 60)) % 60
        when {
            diffHours > 0 -> "hace $diffHours h $diffMinutes min"
            diffMinutes > 0 -> "hace $diffMinutes min"
            else -> "hace menos de 1 min"
        }
    } ?: "Sin datos"

    // Simular temperatura del dispositivo
    val deviceTemp = remember { mutableStateOf((32..42).random()) }

    // Enhanced carousel animation
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Stat cards data with resource IDs instead of strings
    val statCardsData = listOf(
        Triple(R.string.optimizations, "${boostHistory.size}", Icons.Default.TrendingUp to Color(0xFF4CAF50)),
        Triple(R.string.avg_fps, "$fps", Icons.Default.PlayArrow to Color(0xFF2196F3)),
        Triple(R.string.ping, "${ping}ms", Icons.Default.NetworkCheck to Color(0xFFFF9800)),
        Triple(R.string.battery_level, "$batteryLevel%", Icons.Default.BatteryFull to Color(0xFFE91E63)),
        Triple(R.string.ram, "${String.format("%.1f", ramUsage)}%", Icons.Default.Memory to Color(0xFF9C27B0)),
        Triple(R.string.storage, "${String.format("%.1f", storageUsage)}%", Icons.Default.Storage to Color(0xFF009688))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 72.dp, top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate("sidebar") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "Sidebar",
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = textColor
                    )
                    Text(
                        text = stringResource(R.string.optimize_experience_tagline),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = { navController.navigate("game_launcher") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.playlauncher),
                        contentDescription = "Game Launcher",
                        tint = Color.Unspecified
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Safe boost switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = safeBoost,
                    onCheckedChange = { viewModel.setSafeBoost(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.safe_boost_mode),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                val snackbarScope = rememberCoroutineScope()
                IconButton(onClick = {
                    snackbarScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Safe boost guarantees that crucial processes won't be closed. No need to worry!"
                        )
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }

            // Boost button
            Box(contentAlignment = Alignment.Center) {
                Button(
                    onClick = {
                        scope.launch {
                            if (!safeBoost) {
                                snackbarHostState.showSnackbar("¡Advertencia! El modo seguro está desactivado.")
                            }
                            isBoosting = true
                            buttonScale = 1.15f
                            viewModel.performBoost()
                            snackbarHostState.showSnackbar("Optimización completada")
                            buttonScale = 1f
                            isBoosting = false
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(16.dp, RoundedCornerShape(75))
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                ) {
                    if (isBoosting) {
                        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("boost_rocket.json"))
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(80.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.boost_now),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Enhanced stats carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(statCardsData) { (titleRes, value, iconData) ->
                        val (icon, color) = iconData
                        StatCard(
                            title = stringResource(titleRes), // Aquí sí funciona porque estamos en contexto Composable
                            value = value,
                            icon = icon,
                            color = color,
                            isDarkTheme = isDarkTheme,
                            offset = offset
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Recommended games section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.recommended_games),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { navController.navigate("game_list") }) {
                            Text(stringResource(R.string.see_all), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recommendedGames.take(6)) { game ->
                            RecommendedGameCard(
                                game = game,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://play.google.com/store/apps/details?id=${game.packageName}")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                },
                                isDarkTheme = isDarkTheme,
                                textColor = textColor
                            )
                        }
                    }
                }
            }

            // Last boost card with icon
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.last_boost_label, lastBoostText),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                }
            }

            // Device temperature card with icon
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.current_temperature, deviceTemp.value),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                }
            }

            // Extra helpful card: Tip to switch
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.switch_to_launcher_hint),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = textColor
                    )
                }
            }

            // Performance metrics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.system_performance),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PerformanceMetric(
                        label = stringResource(R.string.ram),
                        value = "${String.format("%.1f", ramUsage)}%",
                        progress = (ramUsage / 100f).coerceIn(0f, 1f),
                        color = Color(0xFF4CAF50),
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PerformanceMetric(
                        label = stringResource(R.string.storage),
                        value = "${String.format("%.1f", storageUsage)}%",
                        progress = (storageUsage / 100f).coerceIn(0f, 1f),
                        color = Color(0xFF2196F3),
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PerformanceMetric(
                        label = stringResource(R.string.battery),
                        value = "$batteryLevel%",
                        progress = (batteryLevel / 100f).coerceIn(0f,1f),
                        color = Color(0xFFFF9800),
                        textColor = textColor
                    )
                }
            }
        }

        // Bottom navigation
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            NavigationBar(
                containerColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("gaming_news") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.news)
                        )
                    },
                    label = {
                        Text(
                            stringResource(R.string.news),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already in Dashboard */ },
                    icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = stringResource(R.string.dashboard)) },
                    label = { Text(stringResource(R.string.dashboard), fontFamily = Montserrat, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("ai_analysis") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.ai_analysis)) },
                    label = { Text(stringResource(R.string.ai_analysis), fontFamily = Montserrat, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Removed FAB; navigation moved to header icon per UX guidance

        SnackbarHost(hostState = snackbarHostState)
    }

    if (showFeedbackDetails.value) {
        navController.navigate("boost_feedback")
        showFeedbackDetails.value = false
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    isDarkTheme: Boolean,
    offset: Float
) {
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(120.dp)
            .graphicsLayer {
                translationX = offset * 20f
                rotationZ = offset * 5f
                scaleX = 1f - (offset * 0.1f)
                scaleY = 1f - (offset * 0.1f)
            }
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            Text(
                text = title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun RecommendedGameCard(
    game: GameInfo,
    onClick: () -> Unit,
    localIcon: Int? = GameIconUtils.getLocalGameIcon(game.packageName),
    isDarkTheme: Boolean,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF0F0F0)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (localIcon != null) {
                Icon(
                    painter = painterResource(id = localIcon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = game.appName,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                text = game.category,
                fontFamily = Montserrat,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PerformanceMetric(
    label: String,
    value: String,
    progress: Float,
    color: Color,
    textColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textColor
            )
            Text(
                text = value,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.3f)
        )
    }
}