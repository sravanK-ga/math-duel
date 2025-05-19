package com.mathduel.viewmodel

import androidx.lifecycle.ViewModel
import com.mathduel.domain.models.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun updatePlayerName(playerIndex: Int, name: String) {
        _gameState.update { currentState ->
            val updatedPlayers = currentState.players.toMutableList()
            val currentPlayer = updatedPlayers[playerIndex]
            updatedPlayers[playerIndex] = currentPlayer.copy(name = name.trim())
            currentState.copy(players = updatedPlayers)
        }
    }

    fun startGame() {
        val currentState = _gameState.value
        if (currentState.players.all { it.name.isNotBlank() }) {
            _gameState.update { state ->
                state.copy(
                    currentQuestion = generateQuestion(),
                    gameStarted = true,
                    gameEnded = false,
                    questionCount = 0
                )
            }
        }
    }

    fun checkAnswer(playerIndex: Int, selectedAnswer: Int) {
        val currentState = _gameState.value
        val currentQuestion = currentState.currentQuestion ?: return

        if (selectedAnswer == currentQuestion.answer) {
            _gameState.update { state ->
                val updatedPlayers = state.players.toMutableList()
                val currentPlayer = updatedPlayers[playerIndex]
                val newScore = currentPlayer.score + 1
                updatedPlayers[playerIndex] = currentPlayer.copy(score = newScore)

                val isLastQuestion = state.questionCount >= 9
                val newQuestion = if (!isLastQuestion) {
                    generateQuestion()
                } else null

                state.copy(
                    players = updatedPlayers,
                    currentQuestion = newQuestion,
                    questionCount = state.questionCount + 1,
                    gameEnded = isLastQuestion
                )
            }
        }
    }

    fun resetGame() {
        _gameState.update { state ->
            state.copy(
                currentQuestion = generateQuestion(),
                questionCount = 0,
                gameStarted = true,
                gameEnded = false,
                players = state.players.map { it.copy(score = 0) }
            )
        }
    }

    fun resetGameWithNewPlayers() {
        _gameState.update {
            GameState()
        }
    }

    private fun generateQuestion(): Question {
        val num1 = (1..20).random()
        val num2 = (1..20).random()
        val answer = num1 + num2
        val options = generateOptions(answer)
        return Question(
            num1 = num1,
            num2 = num2,
            answer = answer,
            options = options
        )
    }

    private fun generateOptions(correctAnswer: Int): List<Int> {
        val options = mutableListOf(correctAnswer)
        while (options.size < 4) {
            val randomOption = (correctAnswer - 5..correctAnswer + 5).random()
            if (randomOption != correctAnswer && !options.contains(randomOption)) {
                options.add(randomOption)
            }
        }
        return options.shuffled()
    }
}

data class GameState(
    val players: List<Player> = listOf(Player(), Player()),
    val currentQuestion: Question? = null,
    val questionCount: Int = 0,
    val gameStarted: Boolean = false,
    val gameEnded: Boolean = false
)

data class Question(
    val num1: Int,
    val num2: Int,
    val answer: Int,
    val options: List<Int>
) 