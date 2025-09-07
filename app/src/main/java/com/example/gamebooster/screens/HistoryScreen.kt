package com.example.gamebooster.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.*
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.LottieConstants
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    selectedLanguage: String
) {
    val history by viewModel.boostHistory.collectAsState()
    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White

    val isLoading = history.isEmpty()
    val shimmerAlpha = rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value

    // Calcular estadísticas resumidas
    val totalBoosts = history.size
    val totalRamFreed = history.sumOf { it.ramFreed }
    val totalAppsClosed = history.sumOf { it.appsClosed }
    val totalBatterySaved = history.sumOf { it.batterySaved }
    val totalStorageFreed = history.sumOf { it.storageFreed }
    val averageDuration = if (history.isNotEmpty()) history.map { it.duration }.average().toLong() else 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
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
                text = stringResource(R.string.history),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Estadísticas resumidas
        if (history.isNotEmpty()) {
            // Gráfica de evolución
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.ram_cpu_usage),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                    AndroidView(
                        factory = { ctx ->
                            LineChart(ctx).apply {
                                val entriesRam = history.sortedBy { it.timestamp }.mapIndexed { i, v -> Entry(i.toFloat(), v.ramFreed.toFloat()) }
                                val entriesApps = history.sortedBy { it.timestamp }.mapIndexed { i, v -> Entry(i.toFloat(), v.appsClosed.toFloat()) }
                                val dataSetRam = LineDataSet(entriesRam, "RAM").apply { color = android.graphics.Color.BLUE }
                                val dataSetApps = LineDataSet(entriesApps, "Apps").apply { color = android.graphics.Color.MAGENTA }
                                data = LineData(dataSetRam, dataSetApps)
                                description.isEnabled = false
                                legend.isEnabled = true
                                axisRight.isEnabled = false
                                setTouchEnabled(false)
                            }
                        },
                        modifier = Modifier.height(180.dp).fillMaxWidth()
                    )
                    // Mensaje informativo sobre la gráfica
                    Text(
                        text = "Consejo: puedes ampliar la gráfica dando dos toques sobre ella.",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            // Botón para limpiar historial
            Button(
                onClick = { viewModel.clearHistory() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.clear_history), color = Color.White)
            }
        }

        if (isLoading) {
            repeat(5) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .alpha(shimmerAlpha),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
        } else if (history.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay historial de optimizaciones",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Realiza tu primera optimización para ver el historial aquí",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(history.sortedByDescending { it.timestamp }) { entry ->
                    HistoryCard(
                        entry = entry,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    isDarkTheme: Boolean
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = textColor
        )
        Text(
            text = label,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun HistoryCard(
    entry: com.example.gamebooster.model.BoostHistoryEntry,
    isDarkTheme: Boolean
) {
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val date = Date(entry.timestamp)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Optimización ${entry.type}",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )
                Text(
                    text = dateFormat.format(date),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    icon = Icons.Default.Memory,
                    value = "${entry.ramFreed}MB",
                    label = "RAM",
                    color = Color.Green,
                    isDarkTheme = isDarkTheme
                )
                MetricItem(
                    icon = Icons.Default.Apps,
                    value = "${entry.appsClosed}",
                    label = "Apps",
                    color = Color(0xFFFFA500),
                    isDarkTheme = isDarkTheme
                )
                MetricItem(
                    icon = Icons.Default.BatteryChargingFull,
                    value = "${entry.batterySaved}%",
                    label = "Batería",
                    color = Color.Green,
                    isDarkTheme = isDarkTheme
                )
                MetricItem(
                    icon = Icons.Default.Storage,
                    value = "${entry.storageFreed}MB",
                    label = "Almac.",
                    color = Color.Blue,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    isDarkTheme: Boolean
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = textColor
        )
        Text(
            text = label,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}