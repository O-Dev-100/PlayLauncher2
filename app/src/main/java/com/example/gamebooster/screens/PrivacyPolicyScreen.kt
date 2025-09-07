package com.example.gamebooster.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.gamebooster.R
import com.example.gamebooster.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PrivacyPolicyScreen(navController: NavController, isDarkTheme: Boolean) {
    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    
    // Contenido HTML de la política de privacidad
    val privacyPolicyHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    line-height: 1.6;
                    margin: 0;
                    padding: 20px;
                    background-color: ${if (isDarkTheme) "#000000" else "#F5F5E6"};
                    color: ${if (isDarkTheme) "#FFFFFF" else "#000000"};
                }
                .container {
                    max-width: 800px;
                    margin: 0 auto;
                    background-color: ${if (isDarkTheme) "#1A1A1A" else "#FFFFFF"};
                    padding: 30px;
                    border-radius: 12px;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                }
                h1 {
                    color: #2196F3;
                    text-align: center;
                    margin-bottom: 30px;
                    font-size: 28px;
                }
                h2 {
                    color: #FF9800;
                    margin-top: 25px;
                    margin-bottom: 15px;
                    font-size: 20px;
                }
                p {
                    margin-bottom: 15px;
                    text-align: justify;
                }
                .highlight {
                    background-color: ${if (isDarkTheme) "#2A2A2A" else "#F0F0F0"};
                    padding: 15px;
                    border-radius: 8px;
                    margin: 15px 0;
                    border-left: 4px solid #2196F3;
                }
                .contact {
                    background-color: ${if (isDarkTheme) "#1E3A8A" else "#E3F2FD"};
                    padding: 20px;
                    border-radius: 8px;
                    margin-top: 30px;
                    text-align: center;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>🔒 Privacy Policy - GameBooster</h1>
                
                <p><strong>Last update:</strong> ${java.time.LocalDate.now()}</p>
                
                <div class="highlight">
                    <p><strong>GameBooster</strong> is committed to protecting your privacy. This policy explains how we collect, use and protect your personal information.</p>
                </div>
                
                <h2>📱 Information We Collect</h2>
                <p>We collect the following:</p>
                <ul>
                    <li><strong>Device information:</strong> Model, Android version, performance info</li>
                    <li><strong>Installed games:</strong> List of games for optimization</li>
                    <li><strong>Usage data:</strong> Boost and optimization stats</li>
                    <li><strong>Settings:</strong> Theme and language preferences</li>
                </ul>
                
                <h2>🎯 How We Use Information</h2>
                <p>We use your information to:</p>
                <ul>
                    <li>Optimize device performance</li>
                    <li>Provide game analytics</li>
                    <li>Improve boost features</li>
                    <li>Personalize your experience</li>
                    <li>Send relevant notifications</li>
                </ul>
                
                <h2>🔐 Data Protection</h2>
                <p>We implement security measures to protect your data:</p>
                <ul>
                    <li>Encriptación de datos sensibles</li>
                    <li>Almacenamiento local seguro</li>
                    <li>Acceso limitado a datos personales</li>
                    <li>Actualizaciones regulares de seguridad</li>
                </ul>
                
                <h2>📊 Sharing Information</h2>
                <p><strong>We do not sell, rent or share your personal information</strong> with third parties, except:</p>
                <ul>
                    <li>Con tu consentimiento explícito</li>
                    <li>Para cumplir con obligaciones legales</li>
                    <li>Para proteger nuestros derechos y seguridad</li>
                </ul>
                
                <h2>🎮 Specific Features</h2>
                <p><strong>Game Optimization:</strong> We analyze installed games to provide personalized optimization recommendations.</p>
                <p><strong>Notifications:</strong> We send notifications about updates, optimizations and gaming news.</p>
                <p><strong>AI Analysis:</strong> We use intelligent analysis to improve your device performance.</p>
                
                <h2>👤 Your Rights</h2>
                <p>You have the right to:</p>
                <ul>
                    <li>Acceder a tu información personal</li>
                    <li>Corregir datos inexactos</li>
                    <li>Solicitar la eliminación de datos</li>
                    <li>Desactivar notificaciones</li>
                    <li>Exportar tus datos</li>
                </ul>
                
                <h2>🍪 Cookies and similar technologies</h2>
                <p>We use local technologies to improve your experience, including user preferences and app settings.</p>
                
                <h2>🌍 International transfers</h2>
                <p>Your data is processed and stored on secure servers. We may transfer data to other countries with adequate protections.</p>
                
                <h2>📝 Changes</h2>
                <p>We may update this policy occasionally. We will notify you about significant changes.</p>
                
                <div class="contact">
                    <h3>📧 Contact</h3>
                    <p>If you have questions about this policy, contact us:</p>
                    <p><strong>Email:</strong> privacy@gamebooster.app</p>
                    <p><strong>Developer:</strong> GameBooster Team</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header personalizado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                IconButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.privacy_policy),
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )
            }
        }
        
        // WebView con la política de privacidad
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    loadDataWithBaseURL(
                        null,
                        privacyPolicyHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        )
    }
} 