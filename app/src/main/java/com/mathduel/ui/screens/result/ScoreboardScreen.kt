package com.mathduel.ui.screens.result

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.mathduel.R
import com.mathduel.navigation.Screen
import com.mathduel.viewmodel.GameViewModel

@Composable
fun ScoreboardScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    val gameState by viewModel.gameState.collectAsState()
    val players = gameState.players
    val winner = players.maxByOrNull { it.score }
    val loser = players.minByOrNull { it.score }
    val isTie = players.size == 2 && players[0].score == players[1].score && players[0].score > 0

    // Celebration animation
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.celebration)
    )
    val lottieAnimatable = rememberLottieAnimatable()
    LaunchedEffect(composition) {
        lottieAnimatable.animate(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
    }

    // Scale animation for winner
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title with celebration animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { lottieAnimatable.progress },
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "Game Results",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }

        // Winner Display with scale animation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .scale(scale),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isTie) "It's a Tie! 🎉" else "Winner! 🏆",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!isTie && winner != null) {
                    Text(
                        text = winner.name.ifEmpty { "Player ${players.indexOf(winner) + 1}" },
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Score: ${winner.score}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Loser Display
        if (!isTie && loser != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Runner Up 🥈",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = loser.name.ifEmpty { "Player ${players.indexOf(loser) + 1}" },
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Score: ${loser.score}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Scores Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Final Scores 📊",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                players.forEach { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (player == winner) 
                                MaterialTheme.colorScheme.primaryContainer
                            else if (player == loser) 
                                MaterialTheme.colorScheme.errorContainer
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = player.name.ifEmpty { "Player ${players.indexOf(player) + 1}" },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (player == winner) 
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else if (player == loser) 
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                                if (player == winner) {
                                    Text(
                                        text = "🏆 Champion",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else if (player == loser) {
                                    Text(
                                        text = "🥈 Runner Up",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                            Text(
                                text = "${player.score} points",
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (player == winner) 
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else if (player == loser) 
                                    MaterialTheme.colorScheme.onErrorContainer
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // New Round Button
            Button(
                onClick = { 
                    viewModel.resetGame()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("New Round")
            }

            // Home Button
            Button(
                onClick = { 
                    viewModel.resetGameWithNewPlayers()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Home")
            }
        }
    }
} 