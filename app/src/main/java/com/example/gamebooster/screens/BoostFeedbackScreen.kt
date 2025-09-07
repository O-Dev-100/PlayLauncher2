package com.example.gamebooster.screens

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gamebooster.R
import com.example.gamebooster.ui.theme.Montserrat
import com.example.gamebooster.viewmodel.BoosterViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.Description

@Composable
fun BoostFeedbackScreen(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val lastBoost by viewModel.lastBoostFeedback.collectAsState()
    val boostHistory by viewModel.boostHistory.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val primaryColor = if (isDarkTheme) Color(0xFF4CAF50) else Color(0xFF2E7D32)

    if (lastBoost == null && !isOptimizing) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val boostTips = viewModel.getBoostTips()
    val progressAnimation = rememberInfiniteTransition()
    val progressAlpha by progressAnimation.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (isOptimizing) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    color = primaryColor,
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.optimizing),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            }
        } else {
            val boost = lastBoost!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
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
                        Text(
                            text = stringResource(R.string.boost_feedback_title),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = textColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Puntuación de Optimización",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { boost.optimizationScore / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .alpha(progressAlpha),
                                color = primaryColor,
                                trackColor = primaryColor.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "${boost.optimizationScore}/100",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = primaryColor,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.boost_results),
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ResultItem(
                                icon = Icons.Default.Memory,
                                color = Color(0xFF4CAF50),
                                label = "RAM Liberada",
                                value = "${boost.ramFreed} MB",
                                textColor = textColor
                            )
                            ResultItem(
                                icon = Icons.Default.Storage,
                                color = Color(0xFF2196F3),
                                label = "Almacenamiento Liberado",
                                value = "${boost.storageFreed} MB",
                                textColor = textColor
                            )
                            ResultItem(
                                icon = Icons.Default.BatteryFull,
                                color = Color(0xFFFF9800),
                                label = "Ahorro de Batería",
                                value = "${boost.batterySaved}%",
                                textColor = textColor
                            )
                            ResultItem(
                                icon = Icons.Default.Thermostat,
                                color = Color(0xFFE91E63),
                                label = "Reducción de Temperatura",
                                value = "${String.format("%.1f", boost.cpuTempReduced)}°C",
                                textColor = textColor
                            )
                            ResultItem(
                                icon = Icons.Default.NetworkCheck,
                                color = Color(0xFF9C27B0),
                                label = "Reducción de Latencia",
                                value = "${boost.networkLatencyReduced}ms",
                                textColor = textColor
                            )
                            ResultItem(
                                icon = Icons.Default.Timer,
                                color = Color(0xFF009688),
                                label = "Duración",
                                value = "${boost.duration / 1000} s",
                                textColor = textColor
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = stringResource(R.string.closed_apps),
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (boost.appsClosedList.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_apps_closed),
                                    color = textColor.copy(alpha = 0.7f),
                                    fontFamily = Montserrat,
                                    fontSize = 14.sp
                                )
                            } else {
                                boost.appsClosedList.forEach { packageName ->
                                    val app = installedApps.find { it.packageName == packageName }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                coroutineScope.launch {
                                                    viewModel.launchApp(packageName)
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = app?.icon?.let { painterResource(id = R.drawable.ic_launcher_foreground) }
                                                ?: painterResource(id = R.drawable.ic_launcher_foreground),
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = app?.appName ?: packageName,
                                            color = textColor,
                                            fontFamily = Montserrat,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = {
                                            coroutineScope.launch {
                                                viewModel.launchApp(packageName)
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Relaunch App",
                                                tint = primaryColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    if (boostTips.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Consejos de Optimización",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                boostTips.forEach { tip ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFF2196F3),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = tip,
                                            color = textColor.copy(alpha = 0.7f),
                                            fontFamily = Montserrat,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    if (boostHistory.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Tendencia de Optimización",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                AndroidView(
                                    factory = { ctx ->
                                        LineChart(ctx).apply {
                                            val entries = boostHistory.take(5).mapIndexed { i, entry ->
                                                Entry(i.toFloat(), entry.optimizationScore.toFloat())
                                            }
                                            val dataSet = LineDataSet(entries, "Puntuación de Optimización").apply {
                                                color = android.graphics.Color.parseColor("#4CAF50")
                                                valueTextColor = android.graphics.Color.BLACK
                                                lineWidth = 2f
                                                setDrawCircles(true)
                                            }
                                            data = LineData(dataSet)
                                            description = Description().apply { text = "Últimas 5 optimizaciones" }
                                            layoutParams = android.view.ViewGroup.LayoutParams(
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 300
                                            )
                                        }
                                    },
                                    modifier = Modifier.height(200.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.performBoost()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reintentar Optimización",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val shareText = """
                                        ¡Acabo de optimizar mi dispositivo con ${context.getString(R.string.app_name)}!
                                        Resultados:
                                        - RAM Liberada: ${boost.ramFreed} MB
                                        - Almacenamiento Liberado: ${boost.storageFreed} MB
                                        - Apps Cerradas: ${boost.appsClosedList.size}
                                        - Puntuación: ${boost.optimizationScore}/100
                                    """.trimIndent()
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir resultados"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Compartir Resultados",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
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
fun ResultItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    label: String,
    value: String,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$label: $value",
            color = textColor,
            fontFamily = Montserrat,
            fontSize = 14.sp
        )
    }
}