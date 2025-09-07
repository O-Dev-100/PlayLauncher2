package com.example.gamebooster.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.gamebooster.R
import com.example.gamebooster.ui.theme.Montserrat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp

@Composable
fun GamingNewsScreen(isDarkTheme: Boolean, selectedLanguage: String) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var newsList by remember { mutableStateOf<List<GamingNews>>(emptyList()) }
    var trendingNews by remember { mutableStateOf<List<GamingNews>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val apiKey = "f20fa6ce1acd7ad1550d77ae8accacaf"
    val lang = if (selectedLanguage == "Spanish") "es" else "en"
    
    // URLs para diferentes tipos de noticias
    val forYouUrl = "https://gnews.io/api/v4/search?q=gaming+esports&lang=$lang&token=$apiKey&max=10"
    val trendingUrl = "https://gnews.io/api/v4/top-headlines?category=technology&lang=$lang&token=$apiKey&max=10"

    LaunchedEffect(selectedLanguage) {
        isLoading = true
        error = null
        try {
            // Si no hay internet, salimos con error controlado
            try {
                java.net.InetAddress.getByName("8.8.8.8")
            } catch (e: Exception) {
                error = context.getString(R.string.news_load_error)
                isLoading = false
                return@LaunchedEffect
            }

            // Cargar noticias "Para ti"
            val forYouResult = withContext(Dispatchers.IO) {
                URL(forYouUrl).readText()
            }
            val forYouJson = JSONObject(forYouResult)
            val forYouArticles = forYouJson.getJSONArray("articles")
            val forYouList = mutableListOf<GamingNews>()
            for (i in 0 until forYouArticles.length()) {
                val item = forYouArticles.getJSONObject(i)
                forYouList.add(
                    GamingNews(
                        title = item.getString("title"),
                        description = item.optString("description", ""),
                        url = item.getString("url"),
                        image = item.optString("image", "")
                    )
                )
            }
            newsList = forYouList

            // Cargar noticias "Trending"
            val trendingResult = withContext(Dispatchers.IO) {
                URL(trendingUrl).readText()
            }
            val trendingJson = JSONObject(trendingResult)
            val trendingArticles = trendingJson.getJSONArray("articles")
            val trendingList = mutableListOf<GamingNews>()
            for (i in 0 until trendingArticles.length()) {
                val item = trendingArticles.getJSONObject(i)
                trendingList.add(
                    GamingNews(
                        title = item.getString("title"),
                        description = item.optString("description", ""),
                        url = item.getString("url"),
                        image = item.optString("image", "")
                    )
                )
            }
            trendingNews = trendingList
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Error"
        }
        isLoading = false
    }

    val backgroundColor = if (isDarkTheme) Color.Black else Color(0xFFF5F5E6)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White

    // Asegurar visibilidad de iconos y texto en modo oscuro
    val trendingIcon = painterResource(id = R.drawable.ic_trending)
    val forYouIcon = painterResource(id = R.drawable.ic_star)
    val trendingTextColor = if (isDarkTheme) Color(0xFFFFA726) else Color(0xFFD84315)
    val forYouTextColor = if (isDarkTheme) Color(0xFF42A5F5) else Color(0xFF1976D2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.gaming_news),
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Pestañas mejoradas con iconos visibles
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = cardColor,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = forYouIcon,
                            contentDescription = "Para ti",
                            tint = if (selectedTab == 0) forYouTextColor else textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Para ti",
                            fontFamily = Montserrat,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) forYouTextColor else textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = trendingIcon,
                            contentDescription = "Trending",
                            tint = if (selectedTab == 1) trendingTextColor else textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trending",
                            fontFamily = Montserrat,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) trendingTextColor else textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            )
        }

        if (isLoading) {
            val shimmerAlpha = rememberInfiniteTransition().animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            ).value
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(5) {
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
                                    .size(64.dp)
                                    .background(
                                        Color.Gray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .height(18.dp)
                                        .fillMaxWidth(0.7f)
                                        .background(
                                            Color.Gray.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .height(14.dp)
                                        .fillMaxWidth(0.5f)
                                        .background(
                                            Color.Gray.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        } else if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.news_load_error),
                        color = Color.Red,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error ?: stringResource(R.string.unknown_error),
                        color = textColor.copy(alpha = 0.7f),
                        fontFamily = Montserrat,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            val currentNewsList = if (selectedTab == 0) newsList else trendingNews
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(currentNewsList) { news ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.url))
                                context.startActivity(intent)
                            },
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (news.image.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(news.image),
                                    contentDescription = news.title,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(
                                            if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF0F0F0),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = news.title,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = textColor
                                )
                                if (news.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = news.description,
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = textColor.copy(alpha = 0.7f),
                                        maxLines = 2
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Default.Star else Icons.Default.TrendingUp,
                                        contentDescription = if (selectedTab == 0) stringResource(R.string.for_you_tab) else stringResource(R.string.trending_tab),
                                        tint = if (selectedTab == 0) forYouTextColor else trendingTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (selectedTab == 0) stringResource(R.string.recommended_badge) else stringResource(R.string.trending_badge),
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = if (selectedTab == 0) forYouTextColor else trendingTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class GamingNews(
    val title: String,
    val description: String,
    val url: String,
    val image: String
) 