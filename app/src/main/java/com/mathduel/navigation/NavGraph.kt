package com.mathduel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mathduel.ui.screens.game.GameScreen
import com.mathduel.ui.screens.home.HomeScreen
import com.mathduel.ui.screens.result.ScoreboardScreen
import com.mathduel.viewmodel.GameViewModel
import androidx.hilt.navigation.compose.hiltViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game")
    object Scoreboard : Screen("scoreboard")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: GameViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Game.route) {
            GameScreen(navController, viewModel)
        }
        composable(Screen.Scoreboard.route) {
            ScoreboardScreen(navController, viewModel)
        }
    }
} 