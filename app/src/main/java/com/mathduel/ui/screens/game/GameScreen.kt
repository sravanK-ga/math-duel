package com.mathduel.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mathduel.navigation.Screen
import com.mathduel.viewmodel.GameViewModel

@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel,
    roomId: String,
    playerId: String
) {
    val gameState by viewModel.gameState.collectAsState()

    // Initialize game when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.initializeGame(roomId, playerId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game Header
        Text(
            text = when {
                gameState.waitingForPlayers -> "Waiting for opponent..."
                gameState.gameEnded -> "Game Over!"
                else -> "Question ${gameState.currentQuestionIndex + 1}/10"
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Player Scores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            gameState.players.forEach { player ->
                val isCurrentPlayer = player.id == playerId
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isCurrentPlayer) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrentPlayer) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Score: ${player.score}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrentPlayer) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Game Content
        if (gameState.waitingForPlayers) {
            CircularProgressIndicator()
        } else if (gameState.gameEnded) {
            GameOverContent(navController, viewModel)
        } else {
            gameState.questions.getOrNull(gameState.currentQuestionIndex)?.let { question ->
                QuestionContent(question, viewModel)
            }
        }
    }
}

@Composable
private fun QuestionContent(
    question: com.mathduel.domain.models.Question,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Question
        Text(
            text = "${question.a} + ${question.b} = ?",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        // Answer Options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            question.options.forEach { option ->
                Button(
                    onClick = { viewModel.checkAnswer(option) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = option.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun GameOverContent(
    navController: NavController,
    viewModel: GameViewModel
) {
    val gameState by viewModel.gameState.collectAsState()
    val players = gameState.players
    val winner = players.maxByOrNull { it.score }
    val isTie = players.size == 2 && players[0].score == players[1].score && players[0].score > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Winner Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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

        // Final Scores
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player.name.ifEmpty { "Player ${players.indexOf(player) + 1}" },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = player.score.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Home Button
        Button(
            onClick = { 
                viewModel.resetGameWithNewPlayers()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Return to Home")
        }
    }
} 