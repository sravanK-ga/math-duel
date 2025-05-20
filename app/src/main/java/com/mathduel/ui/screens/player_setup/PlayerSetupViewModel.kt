package com.mathduel.ui.screens.player_setup

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mathduel.data.models.FirebasePlayer
import com.mathduel.data.models.FirebaseRoom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerSetupViewModel @Inject constructor() : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val roomsRef = database.getReference("rooms")

    private val _uiState = MutableStateFlow<PlayerSetupUiState>(PlayerSetupUiState.Initial)
    val uiState: StateFlow<PlayerSetupUiState> = _uiState

    fun joinGame(playerName: String, onSuccess: (Boolean, String, String) -> Unit) {
        _uiState.value = PlayerSetupUiState.Loading

        // First, try to find a waiting room
        roomsRef.orderByChild("status")
            .equalTo("waiting")
            .limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Join existing room
                        val room = snapshot.children.first()
                        val roomId = room.key ?: return
                        joinExistingRoom(roomId, playerName, onSuccess)
                    } else {
                        // Create new room
                        createNewRoom(playerName, onSuccess)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _uiState.value = PlayerSetupUiState.Error(error.message)
                    onSuccess(false, "", "")
                }
            })
    }

    private fun joinExistingRoom(roomId: String, playerName: String, onSuccess: (Boolean, String, String) -> Unit) {
        val playerId = roomsRef.push().key ?: return
        val playerData = FirebasePlayer(
            name = playerName,
            score = 0
        )

        // Create a map of updates
        val updates = mapOf(
            "/players/$playerId" to playerData,
            "/status" to "active"
        )

        // Update the room directly using the room ID
        roomsRef.child(roomId).updateChildren(updates as Map<String, Any>)
            .addOnSuccessListener {
                _uiState.value = PlayerSetupUiState.Success
                onSuccess(true, roomId, playerId)
            }
            .addOnFailureListener { e ->
                _uiState.value = PlayerSetupUiState.Error(e.message ?: "Failed to join room")
                onSuccess(false, "", "")
            }
    }

    private fun createNewRoom(playerName: String, onSuccess: (Boolean, String, String) -> Unit) {
        val roomId = roomsRef.push().key ?: return
        val playerId = roomsRef.push().key ?: return

        val roomData = FirebaseRoom(
            createdAt = System.currentTimeMillis(),
            status = "waiting",
            players = mapOf(
                playerId to FirebasePlayer(
                    name = playerName,
                    score = 0
                )
            )
        )

        // Create the room directly using the room ID
        roomsRef.child(roomId).setValue(roomData)
            .addOnSuccessListener {
                _uiState.value = PlayerSetupUiState.Success
                onSuccess(true, roomId, playerId)
            }
            .addOnFailureListener { e ->
                _uiState.value = PlayerSetupUiState.Error(e.message ?: "Failed to create room")
                onSuccess(false, "", "")
            }
    }
}

sealed class PlayerSetupUiState {
    object Initial : PlayerSetupUiState()
    object Loading : PlayerSetupUiState()
    object Success : PlayerSetupUiState()
    data class Error(val message: String) : PlayerSetupUiState()
} 