package com.mathduel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mathduel.ui.screens.game.GameScreen
import com.mathduel.ui.screens.home.HomeScreen
import com.mathduel.ui.screens.player_setup.PlayerSetupScreen
import com.mathduel.ui.screens.result.ScoreboardScreen
import com.mathduel.viewmodel.GameViewModel
import androidx.hilt.navigation.compose.hiltViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PlayerSetup : Screen("player_setup")
    object Game : Screen("game/{roomId}/{playerId}") {
        fun createRoute(roomId: String, playerId: String) = "game/$roomId/$playerId"
    }
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
        composable(Screen.PlayerSetup.route) {
            PlayerSetupScreen(
                onNavigateToGame = { roomId, playerId ->
                    navController.navigate(Screen.Game.createRoute(roomId, playerId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                androidx.navigation.navArgument("roomId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("playerId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            val playerId = backStackEntry.arguments?.getString("playerId") ?: return@composable
            GameScreen(
                navController = navController,
                viewModel = viewModel,
                roomId = roomId,
                playerId = playerId
            )
        }
        composable(Screen.Scoreboard.route) {
            ScoreboardScreen(navController, viewModel)
        }
    }
} 