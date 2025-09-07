package com.example.gamebooster.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable as animatedComposable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamebooster.screens.*
import com.example.gamebooster.viewmodel.BoosterViewModel
import com.example.gamebooster.screens.ThemeSelectorScreen
import com.example.gamebooster.viewmodel.AppSettingsViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    boosterViewModel: BoosterViewModel,
    isDarkTheme: Boolean,
    selectedLanguage: String,
    onThemeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val appSettingsViewModel: AppSettingsViewModel = viewModel<AppSettingsViewModel>()
    AnimatedNavHost(
        navController = navController,
        startDestination = "game_launcher",
        enterTransition = { fadeIn(animationSpec = tween(350)) + slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(350)) },
        exitTransition = { fadeOut(animationSpec = tween(350)) + slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(350)) },
        popEnterTransition = { fadeIn(animationSpec = tween(350)) + slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(350)) },
        popExitTransition = { fadeOut(animationSpec = tween(350)) + slideOutHorizontally(targetOffsetX = { it / 4 }, animationSpec = tween(350)) }
    ) {
        animatedComposable("welcome") {
            WelcomeScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = { onThemeChange(!isDarkTheme) },
                selectedLanguage = selectedLanguage,
                onLanguageChange = onLanguageChange
            )
        }
        animatedComposable("dashboard") {
            DashboardScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage
            )
        }
        animatedComposable("stats") {
            StatsScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage,
                onThemeChange = onThemeChange,
                onLanguageChange = onLanguageChange
            )
        }
        animatedComposable("ai_analysis") {
            AIAnalysisScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage,
                onThemeChange = onThemeChange,
                onLanguageChange = onLanguageChange
            )
        }
        animatedComposable("gfx_tool") {
            GFXToolScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage,
                onThemeChange = onThemeChange,
                onLanguageChange = onLanguageChange
            )
        }
        animatedComposable("history") {
            HistoryScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage
            )
        }
        animatedComposable("sidebar") {
            SidebarScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage,
                onThemeChange = onThemeChange,
                onLanguageChange = onLanguageChange
            )
        }
        animatedComposable("support") {
            SupportScreen(
                navController = navController,
                viewModel = boosterViewModel
            )
        }
        animatedComposable("game_list") {
            GameListScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage
            )
        }
        animatedComposable("boost_configs") {
            BoostConfigsScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage
            )
        }
        animatedComposable("optimize") {
            OptimizeScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage
            )
        }
        animatedComposable("gaming_news") {
            GamingNewsScreen(
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage
            )
        }
        animatedComposable("quick_access") {
            QuickAccessScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage
            )
        }
        animatedComposable("privacy_policy") { PrivacyPolicyScreen(navController, isDarkTheme) }
        animatedComposable("notifications") {
            NotificationsScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                selectedLanguage = selectedLanguage,
                onThemeChange = onThemeChange,
                onLanguageChange = onLanguageChange
            )
        }
        animatedComposable("feedback") {
            FeedbackScreen(
                navController = navController,
                isDarkTheme = isDarkTheme
            )
        }
        animatedComposable("achievements") {
            AchievementsScreen(
                navController = navController,
                isDarkTheme = isDarkTheme
            )
        }
        animatedComposable("theme_selector") {
            ThemeSelectorScreen(navController = navController, appSettingsViewModel = appSettingsViewModel)
        }
        animatedComposable("boost_feedback") {
            BoostFeedbackScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme
            )
        }
        animatedComposable("game_launcher") {
            GameLauncherScreen(
                navController = navController,
                viewModel = boosterViewModel,
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange
            )
        }
    }
}