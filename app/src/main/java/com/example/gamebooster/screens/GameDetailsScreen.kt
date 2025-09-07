package com.example.gamebooster.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.gamebooster.R
import com.example.gamebooster.data.model.GameDetails
import com.example.gamebooster.ui.theme.Montserrat
import com.example.gamebooster.ui.theme.primaryColor
import com.example.gamebooster.ui.theme.secondaryColor
import com.example.gamebooster.utils.formatNumber
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(
    gameDetails: GameDetails,
    onClose: () -> Unit,
    onPlay: () -> Unit,
    onBoostAndPlay: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color.White
    val surfaceColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryTextColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF757575
    )

    var selectedImageIndex by remember { mutableStateOf(0) }
    val screenshots = gameDetails.screenshots ?: emptyList()
    
    // Add header image as first screenshot if available
    val allImages = remember(gameDetails.headerImage, screenshots) {
        val images = mutableListOf<String>()
        gameDetails.headerImage?.let { images.add(it) }
        images.addAll(screenshots)
        images
    }

    // Show image viewer dialog when an image is selected
    var showImageDialog by remember { mutableStateOf(false) }
    var currentImageIndex by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with back button and game image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                // Game header image
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(gameDetails.headerImage ?: gameDetails.icon)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(id = R.drawable.ic_game_placeholder),
                    error = painterResource(id = R.drawable.ic_game_placeholder)
                )
                
                Image(
                    painter = painter,
                    contentDescription = gameDetails.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = 0.8f
                        }
                )
                
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    backgroundColor.copy(alpha = 0.7f),
                                    backgroundColor
                                ),
                                startY = 0f,
                                endY = 250f
                            )
                        )
                )
                
                // Back button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Game icon and title at the bottom of the header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Game icon
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = gameDetails.icon,
                            placeholder = painterResource(id = R.drawable.ic_game_placeholder),
                            error = painterResource(id = R.drawable.ic_game_placeholder)
                        ),
                        contentDescription = "${gameDetails.title} icon",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Game title and rating
                    Column {
                        Text(
                            text = gameDetails.title,
                            color = textColor,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            maxLines = 2,
                            lineHeight = 24.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = "${gameDetails.scoreText} • ${formatNumber(gameDetails.ratings)} ratings",
                                color = secondaryTextColor,
                                fontFamily = Montserrat,
                                fontSize = 14.sp
                            )
                        }
                        
                        // Downloads
                        Text(
                            text = gameDetails.installs,
                            color = secondaryTextColor,
                            fontFamily = Montserrat,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play button
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "PLAY",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Boost & Play button
                Button(
                    onClick = onBoostAndPlay,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = secondaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "BOOST & PLAY",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Screenshots section
            if (allImages.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Screenshots",
                        color = textColor,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allImages) { imageUrl ->
                            val index = allImages.indexOf(imageUrl)
                            Box(
                                modifier = Modifier
                                    .size(160.dp, 280.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        currentImageIndex = index
                                        showImageDialog = true
                                    }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = imageUrl,
                                        placeholder = painterResource(id = R.drawable.ic_image_placeholder),
                                        error = painterResource(id = R.drawable.ic_image_placeholder)
                                    ),
                                    contentDescription = "Screenshot ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
            
            // Game info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // About section
                Text(
                    text = "About",
                    color = textColor,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Game description
                Text(
                    text = gameDetails.summary.ifEmpty { "No description available" },
                    color = textColor.copy(alpha = 0.8f),
                    fontFamily = Montserrat,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Additional info
                InfoRow(
                    label = "Category",
                    value = gameDetails.genre,
                    icon = R.drawable.ic_category,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor
                )
                
                InfoRow(
                    label = "Size",
                    value = gameDetails.size,
                    icon = R.drawable.ic_storage,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor
                )
                
                if (!gameDetails.version.isNullOrEmpty()) {
                    InfoRow(
                        label = "Version",
                        value = gameDetails.version,
                        icon = R.drawable.ic_info,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
                
                if (!gameDetails.androidVersionText.isNullOrEmpty()) {
                    InfoRow(
                        label = "Requires Android",
                        value = gameDetails.androidVersionText,
                        icon = R.drawable.ic_android,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
                
                if (gameDetails.contentRating != null) {
                    InfoRow(
                        label = "Content Rating",
                        value = gameDetails.contentRating,
                        icon = R.drawable.ic_age_rating,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
                
                if (gameDetails.updated > 0) {
                    InfoRow(
                        label = "Updated on",
                        value = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date(gameDetails.updated)),
                        icon = R.drawable.ic_update,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
                
                // Developer info
                Text(
                    text = "Developer",
                    color = textColor,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Text(
                    text = gameDetails.packageName,
                    color = secondaryTextColor,
                    fontFamily = Montserrat,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        
        // Image viewer dialog
        if (showImageDialog && allImages.isNotEmpty()) {
            Dialog(
                onDismissRequest = { showImageDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .clickable { showImageDialog = false }
                ) {
                    // Close button
                    IconButton(
                        onClick = { showImageDialog = false },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(40.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Image
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = allImages[currentImageIndex],
                            error = painterResource(id = R.drawable.ic_image_placeholder)
                        ),
                        contentDescription = "Screenshot ${currentImageIndex + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f)
                            .align(Alignment.Center)
                            .clickable { /* Handle click on image if needed */ }
                    )
                    
                    // Image indicator
                    if (allImages.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            allImages.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(
                                            if (index == currentImageIndex) 12.dp else 8.dp,
                                            8.dp
                                        )
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentImageIndex) Color.White else Color.White.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                        .clickable {
                                            currentImageIndex = index
                                        }
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
private fun InfoRow(
    label: String,
    value: String?,
    @androidx.annotation.DrawableRes icon: Int,
    textColor: Color,
    secondaryTextColor: Color
) {
    if (value.isNullOrEmpty()) return
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = secondaryTextColor,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = label,
                color = secondaryTextColor,
                fontFamily = Montserrat,
                fontSize = 12.sp
            )
            
            Text(
                text = value,
                color = textColor,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Add these extension functions to a new file if they don't exist
@Composable
fun AnimatedFadeIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(initialAlpha = 0.4f),
        exit = fadeOut(animationSpec = tween(durationMillis = 100)),
        modifier = modifier
    ) {
        content()
    }
}

fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> "${(number / 1_000_000f).roundToInt()}M"
        number >= 1_000 -> "${(number / 1_000f).roundToInt()}K"
        else -> number.toString()
    }
}
