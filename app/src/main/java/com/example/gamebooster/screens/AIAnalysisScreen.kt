package com.example.gamebooster.screens

import android.content.Context
import android.graphics.Color as GColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.example.gamebooster.R
import com.example.gamebooster.viewmodel.BoosterViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.gamebooster.ui.theme.Montserrat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalysisScreen(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    selectedLanguage: String,
    onThemeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val ramUsage by viewModel.ramUsage.collectAsState()
    val cpuTemp by viewModel.cpuTemp.collectAsState()
    val fps by viewModel.fps.collectAsState()
    val ping by viewModel.ping.collectAsState()
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var aiRecommendations by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    val prefs = context.getSharedPreferences("ai_advanced_prefs", Context.MODE_PRIVATE)
    var aggressiveOptimization by remember { mutableStateOf(prefs.getBoolean("aggressiveOptimization", false)) }
    var limitBackgroundApps by remember { mutableStateOf(prefs.getBoolean("limitBackgroundApps", false)) }
    var optimizeNetwork by remember { mutableStateOf(prefs.getBoolean("optimizeNetwork", false)) }
    val autoOptimize by viewModel.autoOptimize.collectAsState()

    fun saveAdvancedPrefs() {
        prefs.edit()
            .putBoolean("aggressiveOptimization", aggressiveOptimization)
            .putBoolean("limitBackgroundApps", limitBackgroundApps)
            .putBoolean("optimizeNetwork", optimizeNetwork)
            .apply()
        viewModel.toggleAggressiveMode()
    }

    val ramData = remember { mutableStateListOf<Float>() }
    val cpuData = remember { mutableStateListOf<Float>() }
    val fpsData = remember { mutableStateListOf<Float>() }
    val pingData = remember { mutableStateListOf<Float>() }

    LaunchedEffect(ramUsage, cpuTemp, fps, ping) {
        if (ramData.size >= 8) ramData.removeAt(0)
        if (cpuData.size >= 8) cpuData.removeAt(0)
        if (fpsData.size >= 8) fpsData.removeAt(0)
        if (pingData.size >= 8) pingData.removeAt(0)
        ramData.add(ramUsage)
        cpuData.add(cpuTemp)
        fpsData.add(fps.toFloat())
        pingData.add(ping.toFloat())
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
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                LanguageDropdown(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageChange,
                    isDarkTheme = isDarkTheme
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onThemeChange(!isDarkTheme) }) {
                    Icon(
                        painter = painterResource(id = if (isDarkTheme) R.drawable.ic_sun else R.drawable.ic_moon),
                        contentDescription = stringResource(R.string.toggle_theme)
                    )
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.analysis_history)) },
                        onClick = {
                            showMenu = false
                            navController.navigate("history")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.advanced_settings)) },
                        onClick = {
                            showMenu = false
                            navController.navigate("boost_configs")
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    Brush.verticalGradient(
                        colors = if (isDarkTheme)
                            listOf(Color(0xFF232526), Color(0xFF414345), Color(0xFF232526))
                        else
                            listOf(Color(0xFFF5F5E6), Color(0xFFE0E0D1), Color(0xFFF5F5E6))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .animateContentSize(animationSpec = tween(350)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.ai_analysis),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 18.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnalysisAccessCard(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = stringResource(R.string.stats),
                        onClick = { navController.navigate("stats") },
                        isDarkTheme = isDarkTheme
                    )
                    AnalysisAccessCard(
                        icon = Icons.Default.History,
                        label = stringResource(R.string.history),
                        onClick = { navController.navigate("history") },
                        isDarkTheme = isDarkTheme
                    )
                    AnalysisAccessCard(
                        icon = Icons.Default.Tune,
                        label = stringResource(R.string.advanced_settings),
                        onClick = { navController.navigate("boost_configs") },
                        isDarkTheme = isDarkTheme
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isAnalyzing = true
                                viewModel.performBoost(aggressiveOptimization)
                                delay(2000)
                                val lastFeedback = viewModel.lastBoostFeedback.value
                                analysisResult = lastFeedback?.let { Pair(it.ramFreed, it.appsClosedList.size) }
                                aiRecommendations = viewModel.getBoostTips()
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.analysis_completed, analysisResult?.first ?: 0, analysisResult?.second ?: 0)
                                )
                                isAnalyzing = false
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .padding(8.dp)
                            .height(48.dp)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Analizar ahora",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .shadow(10.dp, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(R.string.performance_metrics),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        AndroidView(
                            factory = { ctx ->
                                LineChart(ctx).apply {
                                    val entriesRam = ramData.mapIndexed { i, v -> Entry(i.toFloat(), v) }
                                    val entriesCpu = cpuData.mapIndexed { i, v -> Entry(i.toFloat(), v) }
                                    val entriesFps = fpsData.mapIndexed { i, v -> Entry(i.toFloat(), v) }
                                    val entriesPing = pingData.mapIndexed { i, v -> Entry(i.toFloat(), v) }
                                    val dataSetRam = LineDataSet(entriesRam, "RAM (%)").apply {
                                        color = GColor.parseColor("#1976D2")
                                        setDrawCircles(false)
                                        lineWidth = 3f
                                    }
                                    val dataSetCpu = LineDataSet(entriesCpu, "CPU Temp (°C)").apply {
                                        color = GColor.parseColor("#E64A19")
                                        setDrawCircles(false)
                                        lineWidth = 3f
                                    }
                                    val dataSetFps = LineDataSet(entriesFps, "FPS").apply {
                                        color = GColor.parseColor("#4CAF50")
                                        setDrawCircles(false)
                                        lineWidth = 3f
                                    }
                                    val dataSetPing = LineDataSet(entriesPing, "Ping (ms)").apply {
                                        color = GColor.parseColor("#9C27B0")
                                        setDrawCircles(false)
                                        lineWidth = 3f
                                    }
                                    data = LineData(dataSetRam, dataSetCpu, dataSetFps, dataSetPing)
                                    description = Description().apply { text = "Métricas en Tiempo Real" }
                                    legend.isEnabled = true
                                    axisRight.isEnabled = false
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT, 400
                                    )
                                }
                            },
                            modifier = Modifier.height(200.dp).fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(R.string.advanced_settings),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = aggressiveOptimization,
                                onCheckedChange = {
                                    aggressiveOptimization = it
                                    saveAdvancedPrefs()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.aggressive_optimization),
                                fontFamily = Montserrat,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = limitBackgroundApps,
                                onCheckedChange = {
                                    limitBackgroundApps = it
                                    saveAdvancedPrefs()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.limit_background_apps),
                                fontFamily = Montserrat,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = optimizeNetwork,
                                onCheckedChange = {
                                    optimizeNetwork = it
                                    saveAdvancedPrefs()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.optimize_network),
                                fontFamily = Montserrat,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = autoOptimize,
                                onCheckedChange = {
                                    viewModel.toggleAutoOptimize()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Optimización Automática",
                                fontFamily = Montserrat,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                if (isAnalyzing) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.analizando),
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else if (analysisResult != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                text = stringResource(R.string.analysis_result),
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = context.getString(R.string.analysis_result_details, analysisResult!!.first, analysisResult!!.second),
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
                if (aiRecommendations.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                text = stringResource(R.string.recommendations),
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            aiRecommendations.forEach { rec ->
                                Text(
                                    text = "• $rec",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun AnalysisAccessCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(90.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}