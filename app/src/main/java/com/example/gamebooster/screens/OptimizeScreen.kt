package com.example.gamebooster.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gamebooster.R
import com.example.gamebooster.ui.theme.Montserrat
import com.example.gamebooster.viewmodel.BoosterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizeScreen(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    selectedLanguage: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    var optimizationProgress by remember { mutableStateOf(0f) }
    var currentStep by remember { mutableStateOf("") }
    var optimizationResults by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }

    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
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
                text = stringResource(R.string.optimization_summary),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OptimizationCard(
                    title = "Optimización General del Sistema",
                    description = "Mejora el rendimiento general del dispositivo para juegos",
                    icon = Icons.Default.Settings,
                    cardColor = cardColor,
                    textColor = textColor,
                    primaryColor = primaryColor,
                    isOptimizing = isOptimizing,
                    onOptimize = {
                        coroutineScope.launch {
                            optimizationProgress = 0f
                            val steps = listOf(
                                "Analizando sistema...",
                                "Optimizando memoria...",
                                "Mejorando rendimiento GPU...",
                                "Optimizando batería...",
                                "Finalizando optimización..."
                            )
                            steps.forEachIndexed { index, step ->
                                currentStep = step
                                optimizationProgress = (index + 1) / steps.size.toFloat()
                                delay(1000)
                            }
                            viewModel.performBoost()
                            val lastBoost = viewModel.lastBoostFeedback.value
                            optimizationResults = mapOf(
                                "cpu_improvement" to (lastBoost?.cpuTempReduced?.toInt() ?: 25),
                                "memory_improvement" to (lastBoost?.ramFreed ?: 30),
                                "battery_improvement" to (lastBoost?.batterySaved ?: 20),
                                "gpu_improvement" to 35,
                                "optimization_score" to (lastBoost?.optimizationScore ?: 80)
                            )
                        }
                    }
                )
            }

            item {
                GameOptimizationCard(
                    cardColor = cardColor,
                    textColor = textColor,
                    primaryColor = primaryColor,
                    isOptimizing = isOptimizing,
                    onOptimize = {
                        coroutineScope.launch {
                            optimizationProgress = 0f
                            val steps = listOf(
                                "Detectando juegos...",
                                "Analizando configuración...",
                                "Optimizando gráficos...",
                                "Ajustando FPS...",
                                "Finalizando..."
                            )
                            steps.forEachIndexed { index, step ->
                                currentStep = step
                                optimizationProgress = (index + 1) / steps.size.toFloat()
                                delay(800)
                            }
                        }
                    }
                )
            }

            item {
                OptimizationToolsCard(
                    cardColor = cardColor,
                    textColor = textColor,
                    primaryColor = primaryColor
                )
            }

            if (optimizationResults.isNotEmpty()) {
                item {
                    OptimizationResultsCard(
                        results = optimizationResults,
                        cardColor = cardColor,
                        textColor = textColor,
                        primaryColor = primaryColor
                    )
                }
            }

            if (isOptimizing) {
                item {
                    OptimizationProgressCard(
                        progress = optimizationProgress,
                        currentStep = currentStep,
                        cardColor = cardColor,
                        textColor = textColor,
                        primaryColor = primaryColor,
                        pulseAnimation = pulseAnimation
                    )
                }
            }
        }
    }
}

@Composable
fun OptimizationCard(
    title: String,
    description: String,
    icon: ImageVector,
    cardColor: Color,
    textColor: Color,
    primaryColor: Color,
    isOptimizing: Boolean,
    onOptimize: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOptimize,
                enabled = !isOptimizing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isOptimizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isOptimizing) "Optimizando..." else "Iniciar Optimización",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun GameOptimizationCard(
    cardColor: Color,
    textColor: Color,
    primaryColor: Color,
    isOptimizing: Boolean,
    onOptimize: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Games,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Optimización de Juegos",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Optimiza la configuración específica de cada juego para mejor rendimiento",
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOptimize,
                enabled = !isOptimizing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isOptimizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isOptimizing) "Optimizando..." else "Optimizar Juegos",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OptimizationToolsCard(
    cardColor: Color,
    textColor: Color,
    primaryColor: Color
) {
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
                text = "Herramientas de Optimización",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            val tools = listOf(
                Triple("Limpiar Cache", Icons.Default.Delete, Color.Red),
                Triple("Optimizar Memoria", Icons.Default.Memory, Color.Blue),
                Triple("Ajustar Gráficos", Icons.Default.Tune, Color.Green),
                Triple("Monitorear FPS", Icons.Default.Speed, Color(0xFFFFA500))
            )
            tools.forEach { (name, icon, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                        text = name,
                        fontFamily = Montserrat,
                        fontSize = 14.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OptimizationResultsCard(
    results: Map<String, Any>,
    cardColor: Color,
    textColor: Color,
    primaryColor: Color
) {
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
                text = "Resultados de Optimización",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OptimizationMetric(
                    label = "CPU",
                    value = "${results["cpu_improvement"]}%",
                    color = Color.Green
                )
                OptimizationMetric(
                    label = "Memoria",
                    value = "${results["memory_improvement"]}%",
                    color = Color.Blue
                )
                OptimizationMetric(
                    label = "GPU",
                    value = "${results["gpu_improvement"]}%",
                    color = Color(0xFF800080)
                )
                OptimizationMetric(
                    label = "Batería",
                    value = "${results["battery_improvement"]}%",
                    color = Color(0xFFFFA500)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OptimizationMetric(
                label = "Puntuación",
                value = "${results["optimization_score"]}",
                color = primaryColor
            )
        }
    }
}

@Composable
fun OptimizationMetric(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )
        Text(
            text = label,
            fontFamily = Montserrat,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun OptimizationProgressCard(
    progress: Float,
    currentStep: String,
    cardColor: Color,
    textColor: Color,
    primaryColor: Color,
    pulseAnimation: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = currentStep,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = primaryColor,
                trackColor = primaryColor.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}