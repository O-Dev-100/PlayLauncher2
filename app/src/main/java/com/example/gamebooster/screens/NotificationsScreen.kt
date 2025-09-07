package com.example.gamebooster.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import com.example.gamebooster.R
import com.example.gamebooster.ui.theme.Montserrat
import com.example.gamebooster.viewmodel.BoosterViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    selectedLanguage: String,
    onThemeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val showNotifications by viewModel.showNotifications.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Simulated notification settings
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var ledEnabled by remember { mutableStateOf(true) }

    val backgroundColor = if (isDarkTheme) Color(0xFF232526) else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val primaryColor = if (isDarkTheme) Color(0xFF4CAF50) else Color(0xFF2E7D32)

    val buttonAlpha by animateFloatAsState(
        targetValue = if (showNotifications) 1f else 0.5f,
        animationSpec = tween(300)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notifications),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkTheme) Color(0xFF232526) else Color(0xFFE0E0D1)
                ),
                actions = {
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
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.notification_settings),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.enable_notifications),
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = showNotifications,
                            onCheckedChange = {
                                viewModel.toggleShowNotifications()
                                if (it) {
                                    requestPostNotificationsPermission(context, snackbarHostState)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = primaryColor,
                                checkedTrackColor = primaryColor.copy(alpha = 0.5f)
                            )
                        )
                    }
                    if (!notificationManager.areNotificationsEnabled()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Debes habilitar el permiso de notificaciones en Ajustes",
                                fontFamily = Montserrat,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.8f),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { openNotificationSettings(context) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_info),
                                    contentDescription = "Abrir ajustes",
                                    tint = primaryColor
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.vibration),
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it },
                            enabled = showNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = primaryColor,
                                checkedTrackColor = primaryColor.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.alpha(buttonAlpha)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.led_indicator),
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = ledEnabled,
                            onCheckedChange = { ledEnabled = it },
                            enabled = showNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = primaryColor,
                                checkedTrackColor = primaryColor.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.alpha(buttonAlpha)
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                val channelId = "test_channel"
                                val channel = NotificationChannel(
                                    channelId,
                                    "Test Notifications",
                                    NotificationManager.IMPORTANCE_DEFAULT
                                ).apply {
                                    enableVibration(vibrationEnabled)
                                    if (ledEnabled) {
                                        enableLights(true)
                                        lightColor = android.graphics.Color.GREEN
                                    }
                                }
                                notificationManager.createNotificationChannel(channel)
                                val notification = NotificationCompat.Builder(context, channelId)
                                    .setSmallIcon(R.drawable.ic_power)
                                    .setContentTitle(context.getString(R.string.app_name))
                                    .setContentText("¡Notificación de prueba enviada! Sonido: $soundEnabled, Vibración: $vibrationEnabled, LED: $ledEnabled")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setAutoCancel(true)
                                    .build()
                                notificationManager.notify(System.currentTimeMillis().toInt(), notification)
                                snackbarHostState.showSnackbar("Notificación de prueba enviada")
                            }
                        },
                        enabled = showNotifications,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .alpha(buttonAlpha)
                    ) {
                        Text(
                            text = stringResource(R.string.test_notification),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun requestPostNotificationsPermission(context: Context, snackbarHostState: SnackbarHostState) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // This should be requested at runtime from an Activity; here we guide the user
            openNotificationSettings(context)
        }
    } catch (_: Exception) { }
}

private fun openNotificationSettings(context: Context) {
    try {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.parse("package:${context.packageName}")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) { }
}