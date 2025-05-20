package com.mathduel.data.models

data class FirebaseRoom(
    val createdAt: Long = 0,
    val status: String = "waiting",
    val players: Map<String, FirebasePlayer> = emptyMap(),
    val questions: Map<String, FirebaseQuestion> = emptyMap(),
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, FirebaseAnswer> = emptyMap()
)

data class FirebasePlayer(
    val name: String = "",
    val score: Int = 0
)

data class FirebaseQuestion(
    val a: Int = 0,
    val b: Int = 0
)

data class FirebaseAnswer(
    val playerId: String = "",
    val correct: Boolean = false,
    val timestamp: Long = 0
) 