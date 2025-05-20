package com.mathduel.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.mathduel.data.models.FirebaseAnswer
import com.mathduel.data.models.FirebasePlayer
import com.mathduel.data.models.FirebaseQuestion
import com.mathduel.data.models.FirebaseRoom
import com.mathduel.domain.models.GameState
import com.mathduel.domain.models.Player
import com.mathduel.domain.models.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val roomsRef = database.getReference("rooms")
    private var currentRoomId: String? = null
    private var currentPlayerId: String? = null

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState

    private var roomListener: ValueEventListener? = null

    fun initializeGame(roomId: String, playerId: String) {
        currentRoomId = roomId
        currentPlayerId = playerId
        setupRoomListener()
    }

    private fun setupRoomListener() {
        val roomId = currentRoomId ?: return
        val roomRef = roomsRef.child(roomId)

        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get individual fields instead of trying to deserialize the whole object
                val status = snapshot.child("status").getValue(String::class.java) ?: "waiting"
                val currentQuestionIndex = snapshot.child("currentQuestionIndex").getValue(Int::class.java) ?: 0
                
                // Get players
                val playersSnapshot = snapshot.child("players")
                val players = mutableListOf<Player>()
                playersSnapshot.children.forEach { playerSnapshot ->
                    val playerId = playerSnapshot.key ?: return@forEach
                    val playerData = playerSnapshot.getValue(FirebasePlayer::class.java) ?: return@forEach
                    players.add(Player(
                        id = playerId,
                        name = playerData.name,
                        score = playerData.score
                    ))
                }

                // Get questions
                val questionsSnapshot = snapshot.child("questions")
                val questions = mutableListOf<Question>()
                questionsSnapshot.children.forEach { questionSnapshot ->
                    val questionData = questionSnapshot.getValue(FirebaseQuestion::class.java) ?: return@forEach
                    questions.add(Question(
                        a = questionData.a,
                        b = questionData.b
                    ))
                }

                // Get answers for current question
                val answersSnapshot = snapshot.child("answers")
                val currentQuestionAnswers = mutableMapOf<String, FirebaseAnswer>()
                answersSnapshot.children.forEach { answerSnapshot ->
                    val answerData = answerSnapshot.getValue(FirebaseAnswer::class.java) ?: return@forEach
                    if (answerData.correct) {
                        currentQuestionAnswers[answerSnapshot.key ?: return@forEach] = answerData
                    }
                }

                // Update game state
                _gameState.value = _gameState.value.copy(
                    roomId = roomId,
                    players = players,
                    questions = questions,
                    currentQuestionIndex = currentQuestionIndex,
                    gameStarted = status == "active",
                    gameEnded = status == "completed",
                    waitingForPlayers = players.size < 2
                )

                // Generate questions if game just started
                if (_gameState.value.gameStarted && questions.isEmpty()) {
                    generateQuestions()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        roomRef.addValueEventListener(roomListener!!)
    }

    private fun generateQuestions() {
        val roomId = currentRoomId ?: return
        val questions = List(10) {
            val a = (1..10).random()
            val b = (1..10).random()
            Question(a, b)
        }

        val questionsMap = questions.mapIndexed { index, question ->
            index.toString() to FirebaseQuestion(
                a = question.a,
                b = question.b
            )
        }.toMap()

        // Update questions directly in the room
        roomsRef.child(roomId).child("questions").setValue(questionsMap)
    }

    fun checkAnswer(answer: Int) {
        val roomId = currentRoomId ?: return
        val playerId = currentPlayerId ?: return
        val currentState = _gameState.value
        val currentQuestion = currentState.questions.getOrNull(currentState.currentQuestionIndex) ?: return

        if (answer == currentQuestion.correctAnswer) {
            // Create answer data
            val answerData = FirebaseAnswer(
                playerId = playerId,
                correct = true,
                timestamp = System.currentTimeMillis()
            )

            // Try to submit the answer
            val answerRef = roomsRef.child(roomId)
                .child("answers")
                .child(currentState.currentQuestionIndex.toString())

            // First check if there's already a correct answer
            answerRef.get().addOnSuccessListener { snapshot ->
                val existingAnswer = snapshot.getValue(FirebaseAnswer::class.java)
                
                // Only proceed if there's no answer yet or this answer is earlier
                if (existingAnswer == null || existingAnswer.timestamp > answerData.timestamp) {
                    // Set the answer
                    answerRef.setValue(answerData).addOnSuccessListener {
                        // Update score
                        val playerRef = roomsRef.child(roomId).child("players").child(playerId)
                        playerRef.child("score").get().addOnSuccessListener { scoreSnapshot ->
                            val currentScore = (scoreSnapshot.value as? Number)?.toInt() ?: 0
                            playerRef.child("score").setValue(currentScore + 1)
                        }

                        // Move to next question
                        val nextIndex = currentState.currentQuestionIndex + 1
                        if (nextIndex >= 10) {
                            // Game over - mark room as completed
                            roomsRef.child(roomId).child("status").setValue("completed")
                        } else {
                            roomsRef.child(roomId).child("currentQuestionIndex").setValue(nextIndex)
                        }
                    }
                }
            }
        }
    }

    fun resetGame() {
        val roomId = currentRoomId ?: return
        // Instead of removing the room, just mark it as completed
        roomsRef.child(roomId).child("status").setValue("completed")
        
        // Reset local state
        currentRoomId = null
        currentPlayerId = null
        _gameState.value = GameState()
    }

    fun resetGameWithNewPlayers() {
        val roomId = currentRoomId ?: return
        // Create a new room with the same ID but fresh data
        val newRoomData = FirebaseRoom(
            createdAt = System.currentTimeMillis(),
            status = "waiting",
            players = emptyMap(),
            questions = emptyMap(),
            currentQuestionIndex = 0,
            answers = emptyMap()
        )
        
        // Update the room with fresh data
        roomsRef.child(roomId).setValue(newRoomData)
        
        // Reset local state
        currentRoomId = null
        currentPlayerId = null
        _gameState.value = GameState()
    }

    override fun onCleared() {
        super.onCleared()
        currentRoomId?.let { roomId ->
            roomListener?.let { listener ->
                roomsRef.child(roomId).removeEventListener(listener)
            }
        }
    }
} 