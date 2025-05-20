###🧾 Product Requirements Document (PRD)
##Project Overview
We're building a native Android application that allows 2 players to play the Math duel game on their own devices. The app handles game setup and show them the score and leaderboard. The game setup will be in such a way that create a room by using the firebase and allow both the players to join the room, when both the players joins the room, then start the game.

###Technology Stack

Language: Kotlin
IDE: Android Studio
Architecture: MVVM
UI Framework: Jetpack Compose
Navigation: Jetpack Navigation Component
Dependency Injection: Hilt
Database: Room (optional, for score/history persistence)
Networking: Retrofit (reserved for future online play)
Concurrency: Kotlin Coroutines
Image Loading: Coil
Design: Material Design
Animation: Lottie
View Handling: ViewModel + LiveData
Build Tools: Gradle
Themes: Material + Custom XML

#####Core Functionalities & Flow

####Step 1: App Initialization & Splash Screen

Display animated splash screen (using Lottie)
Navigate to home screen after delay

####Step 2: Home Screen

UI Components:

Button: "Start New Game"

Behavior:

On click, navigate to Player Setup screen

### Step 3: Player Setup Screen
🎨 UI Components:
EditText: Player Name input field for the user to enter their name.

Button: Proceed button to continue to the game logic.

🔄 Behavior & Flow:
User Enters Name:

The user is required to input their name in the text field.

Optionally, validate that the name is not empty.

On Proceed Button Click:

When the player clicks the Proceed button:

Fetch all existing game rooms from Firebase Realtime Database.

Filter the rooms that are:

Not full (i.e., have less than 2 players).

Are in a "waiting" state.

Among these, find the room with the latest timestamp (i.e., most recently created).

Join or Create Room:

If a suitable room is found:

Add the current player to the room under /rooms/{roomId}/players/{playerId}.

Update the room's player count/status as necessary.

If no suitable room exists:

Create a new room under /rooms/{generatedRoomId}.

Add the current player as the first player.

Set metadata like:

createdAt: current timestamp.

status: "waiting".

players: dictionary/map with this player's info.

Navigation:

Once two players have joined:

Change the room status to active.

Both players should be navigated to the Game Screen.

### Step 4: Game Screen
🎯 Game Objective:
A real-time quiz where two players answer the same 10 math questions (simple addition), and the first correct answer earns 1 point. The game proceeds question-by-question until all 10 are completed.

⚙️ Behavior & Logic:
Question Generation:

Generate 10 random addition questions (e.g., 2 + 5, 8 + 3) server-side or client-side.

Store the questions under /rooms/{roomId}/questions.

Example structure:

json
Copy
Edit
{
  "questions": {
    "0": {"a": 2, "b": 5},
    "1": {"a": 8, "b": 3},
    ...
  }
}
Game Start:

When the room status is updated to "active", both clients observe:

/rooms/{roomId}/currentQuestionIndex

/rooms/{roomId}/scores

/rooms/{roomId}/answers/{questionIndex}

Gameplay Loop (for each question):

Show the same question to both players.

Players choose an answer.

On answer submission:

Check if a correct answer has already been submitted for that question:

If not:

If the submitted answer is correct:

Increase that player's score by 1 under /scores/{playerId}.

Mark the answer under /answers/{questionIndex} with playerId and correctness.

Increment the /currentQuestionIndex so both players see the next question.

If incorrect:

Player sees a retry or continues (based on your design).

If yes (someone already answered correctly):

The UI should immediately proceed to next question for both players using the updated index.

Question Navigation:

Both players are automatically moved to the next question by observing changes to /currentQuestionIndex.

Game Completion:

After 10 questions:

Update room status to completed.

Show final scores to both players.

Optionally display:

Winner (player with higher score).

Option to restart or return to home.

### Firebase Database Structure Suggestion:
json
Copy
Edit
rooms: {
  {roomId}: {
    createdAt: <timestamp>,
    status: "waiting" | "active" | "completed",
    players: {
      player1Id: {
        name: "Alice",
        score: 0
      },
      player2Id: {
        name: "Bob",
        score: 0
      }
    },
    questions: {
      0: {a: 2, b: 3},
      1: {a: 4, b: 7},
      ...
    },
    currentQuestionIndex: 0,
    answers: {
      0: {
        playerId: "player1Id",
        correct: true
      },
      ...
    },
    scores: {
      player1Id: 0,
      player2Id: 0
    }
  }
}


### Edge Cases to Handle:
If a player leaves mid-game, notify the other player and end the game.

Prevent duplicate points for the same question.

Disable input while waiting for the next question to sync.

Debounce or lock answer submission until verified.


####Step 5: Scoreboard

Show:

Scores of individual user.


After game ends:

Show winning Player


####Step 6: Game Loop

After each round, allow players to:

Start new round
Return to home screen


###📁 Project Structure
Minimal but maintainable structure following MVVM + Hilt + Compose guidelines.
src/
└── main/
    ├── java/com/mathduel/
    │   ├── MainActivity.kt                        # Entry point & NavHost
    │   ├── di/
    │   │   └── AppModule.kt                       # Hilt DI config
    │   ├── data/
    │   │   ├── models/
    │   │   │   ├── FirebaseModels.kt             # Firebase data models
    │   │   │   │   ├── FirebaseRoom
    │   │   │   │   ├── FirebasePlayer
    │   │   │   │   ├── FirebaseQuestion
    │   │   │   │   └── FirebaseAnswer
    │   │   │   └── GameModels.kt                  # Domain models
    │   │   │       ├── Question
    │   │   │       ├── Player
    │   │   │       └── GameState
    │   ├── domain/
    │   │   └── models/
    │   │       └── GameModels.kt                  # Shared domain models
    │   ├── ui/
    │   │   ├── screens/
    │   │   │   ├── home/
    │   │   │   │   └── HomeScreen.kt             # Home screen with start game button
    │   │   │   ├── player_setup/
    │   │   │   │   ├── PlayerSetupScreen.kt      # Player name input and room joining
    │   │   │   │   └── PlayerSetupViewModel.kt   # Room creation/joining logic
    │   │   │   ├── game/
    │   │   │   │   └── GameScreen.kt             # Main game screen
    │   │   │   └── scoreboard/
    │   │   │       └── ScoreboardScreen.kt       # Game results screen
    │   │   └── components/
    │   │       └── CommonComponents.kt           # Reusable UI components
    │   ├── viewmodel/
    │   │   └── GameViewModel.kt                  # Game logic and Firebase interactions
    │   ├── navigation/
    │   │   └── NavGraph.kt                       # Navigation setup
    │   └── utils/
    │       └── Extensions.kt                     # Extension functions
    └── res/
        ├── values/
        │   ├── strings.xml
        │   ├── colors.xml
        │   └── themes.xml
        ├── drawable/                             # Icons, buttons
        ├── raw/                                  # Sounds or lottie animations
        └── anim/                                 # Custom animations (if any)

