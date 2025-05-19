###🧾 Product Requirements Document (PRD)
##Project Overview
We're building a native Android application that allows 2 players to play the Math duel game. The app handles game setup and show them the score and leaderboard.

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



####Step 3: Player Setup and Logic Screen

Split screen among two players

UI Components:

Input field: Player Name for each player
Button: "Proceed"

Behavior:

Split the screen among the two players horizontally and take the names of the players by showing the input fields and store their names and show them the questions to add two numbers, same question for each player if one of them answered correctly then change the question to the next one, the questions count will be 10.
Show one of them as a winner in the text field once all the questions were done and store the results.

####Step 4: Scoreboard

Show:

Scores of individual user.


After game ends:

Show winning Player


####Step 5: Game Loop

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
    │   │   ├── local/
    │   │   │   ├── dao/
    │   │   │   │   └── ResultsDao.kt              # (Optional) Room DAO
    │   │   │   └── entities/
    │   │   │       ├── PlayerEntity.kt
    │   │   │       └── ResultEntity.kt
    │   │   ├── models/
    │   │   │   ├── Player.kt                      # UI domain model
    │   │   │   └── Result.kt
    │   │   ├── repository/
    │   │   │   └── GameRepository.kt              # Business logic
    │   │   └── mappers/
    │   │       └── EntityMappers.kt               # Convert between Entity <-> Domain
    │   ├── domain/
    │   │   ├── models/
    │   │   │   ├── Player.kt
    │   │   │   └── Result.kt
    │   ├── ui/
    │   │   ├── screens/
    │   │   │   ├── home/
    │   │   │   │   └── HomeScreen.kt
    │   │   │   ├── game/
    │   │   │   │   └── PlayerSetupAndGameScreen.kt
    │   │   │   ├── result/
    │   │   │   │   └── ResultScreen.kt
    │   │   └── components/
    │   │       └── CommonComponents.kt
    │   ├── viewmodel/
    │   │   ├── GameViewModel.kt
    │   │   └── ui_state/
    │   │       └── GameUiState.kt                 # Encapsulate UI state in one file
    │   └── navigation/
    │   │   └── NavGraph.kt
    │   └── utils/
    │       └── Extensions.kt                      # Any extension functions
    └── res/
        ├── values/
        │   ├── strings.xml
        │   ├── colors.xml
        │   └── themes.xml
        ├── drawable/                              # Icons, buttons
        ├── raw/                                   # Sounds or lottie animations
        └── anim/                                  # Custom animations (if any)

