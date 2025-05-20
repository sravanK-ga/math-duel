package com.mathduel.ui.screens.player_setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PlayerSetupScreen(
    onNavigateToGame: (roomId: String, playerId: String) -> Unit,
    viewModel: PlayerSetupViewModel = hiltViewModel()
) {
    var playerName by remember { mutableStateOf(TextFieldValue()) }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter Your Name",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = playerName,
            onValueChange = { 
                playerName = it
                isError = false
            },
            label = { Text("Player Name") },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text("Please enter your name")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (playerName.text.isBlank()) {
                    isError = true
                } else {
                    viewModel.joinGame(playerName.text) { success, roomId, playerId ->
                        if (success) {
                            onNavigateToGame(roomId, playerId)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Join Game")
        }
    }
} 