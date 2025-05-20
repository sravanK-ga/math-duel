package com.mathduel.domain.models

data class Question(
    val a: Int,
    val b: Int,
    val options: List<Int> = generateOptions(a, b)
) {
    val correctAnswer: Int = a + b

    companion object {
        private fun generateOptions(a: Int, b: Int): List<Int> {
            val correctAnswer = a + b
            val options = mutableSetOf(correctAnswer)
            
            while (options.size < 4) {
                val randomOffset = (-5..5).random()
                val option = correctAnswer + randomOffset
                if (option >= 0) { // Ensure non-negative answers
                    options.add(option)
                }
            }
            
            return options.shuffled()
        }
    }
}

data class Player(
    val id: String,
    val name: String,
    val score: Int = 0
)

data class GameState(
    val roomId: String = "",
    val players: List<Player> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val questions: List<Question> = emptyList(),
    val gameStarted: Boolean = false,
    val gameEnded: Boolean = false,
    val waitingForPlayers: Boolean = true
) 