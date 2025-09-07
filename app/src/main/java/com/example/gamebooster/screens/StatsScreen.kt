package com.example.gamebooster.screens

import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.gamebooster.viewmodel.BoosterViewModel
import com.example.gamebooster.ui.theme.Montserrat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.Description

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    selectedLanguage: String,
    onThemeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val fps by viewModel.fps.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val ramUsage by viewModel.ramUsage.collectAsState()
    val storageUsage by viewModel.storageUsage.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()

    val metricData = remember { mutableStateListOf<Triple<Float, Float, Float>>() } // RAM, Storage, Battery
    LaunchedEffect(ramUsage, storageUsage, batteryLevel) {
        if (metricData.size >= 8) metricData.removeAt(0)
        metricData.add(Triple(ramUsage, storageUsage, batteryLevel.toFloat()))
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isDarkTheme)
                                listOf(Color(0xFF232526), Color(0xFF414345))
                            else
                                listOf(Color(0xFFF5F5E6), Color(0xFFE0E0D1))
                        )
                    )
                    .padding(8.dp),
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
                    text = stringResource(R.string.stats),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Montserrat,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                LanguageDropdown(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageChange,
                    isDarkTheme = isDarkTheme
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onThemeChange(!isDarkTheme) }) {
                    Icon(
                        painter = painterResource(id = if (isDarkTheme) R.drawable.ic_sun else R.drawable.ic_moon),
                        contentDescription = stringResource(R.string.toggle_theme),
                        tint = textColor
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDarkTheme)
                            listOf(Color(0xFF232526), Color(0xFF414345), Color(0xFF232526))
                        else
                            listOf(Color(0xFFF5F5E6), Color(0xFFE0E0D1), Color(0xFFF5F5E6)),
                        tileMode = TileMode.Clamp
                    )
                )
                .padding(innerPadding)
                .padding(20.dp)
                .animateContentSize(animationSpec = tween(350))
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isOptimizing) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.optimizing),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            } else {
                val hayDatos = (fps > 0 || ping > 0 || ramUsage > 0 || storageUsage > 0 || batteryLevel > 0)
                if (hayDatos) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Métricas Actuales",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("FPS: $fps", color = textColor, fontFamily = Montserrat, fontSize = 14.sp)
                            Text("Ping: $ping ms", color = textColor, fontFamily = Montserrat, fontSize = 14.sp)
                            Text("RAM usada: ${String.format("%.1f", ramUsage)}%", color = textColor, fontFamily = Montserrat, fontSize = 14.sp)
                            Text("Almacenamiento usado: ${String.format("%.1f", storageUsage)}%", color = textColor, fontFamily = Montserrat, fontSize = 14.sp)
                            Text("Batería: $batteryLevel%", color = textColor, fontFamily = Montserrat, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Tendencias de Métricas",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            AndroidView(
                                factory = { ctx ->
                                    LineChart(ctx).apply {
                                        val entriesRam = metricData.mapIndexed { i, v -> Entry(i.toFloat(), v.first) }
                                        val entriesStorage = metricData.mapIndexed { i, v -> Entry(i.toFloat(), v.second) }
                                        val entriesBattery = metricData.mapIndexed { i, v -> Entry(i.toFloat(), v.third) }
                                        val dataSetRam = LineDataSet(entriesRam, "RAM (%)").apply {
                                            color = android.graphics.Color.parseColor("#1976D2")
                                            setDrawCircles(false)
                                            lineWidth = 3f
                                        }
                                        val dataSetStorage = LineDataSet(entriesStorage, "Storage (%)").apply {
                                            color = android.graphics.Color.parseColor("#4CAF50")
                                            setDrawCircles(false)
                                            lineWidth = 3f
                                        }
                                        val dataSetBattery = LineDataSet(entriesBattery, "Battery (%)").apply {
                                            color = android.graphics.Color.parseColor("#E64A19")
                                            setDrawCircles(false)
                                            lineWidth = 3f
                                        }
                                        data = LineData(dataSetRam, dataSetStorage, dataSetBattery)
                                        description = Description().apply { text = "Tendencias de Métricas" }
                                        legend.isEnabled = true
                                        axisRight.isEnabled = false
                                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)
                                    }
                                },
                                modifier = Modifier.height(200.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay datos disponibles",
                            color = textColor,
                            fontFamily = Montserrat,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = { viewModel.performBoost(viewModel.aggressiveMode.value) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        text = stringResource(R.string.boost_now),
                        fontSize = 18.sp,
                        fontFamily = Montserrat,
                        color = Color.White
                    )
                }
            }
        }
    }
}