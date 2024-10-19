package com.example.quizapphw

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quizapphw.ui.theme.QuizappHWTheme
import kotlinx.coroutines.delay

data class QuizQuestion(val question: String, val answers: List<String>, val correctAnswer: String)

class MainActivity : ComponentActivity() {
    private var score by mutableStateOf(0)
    private var currentQuestionIndex by mutableStateOf(0)
    private var questions = listOf(
        QuizQuestion("What is the capital of France?", listOf("Paris", "London", "Berlin", "Rome"), "Paris"),
        QuizQuestion("What is 2 + 2?", listOf("3", "4", "5", "6"), "4"),
        QuizQuestion("What is the capital of Japan?", listOf("Tokyo", "Seoul", "Beijing", "Bangkok"), "Tokyo"),
        QuizQuestion("What is the largest planet?", listOf("Earth", "Mars", "Jupiter", "Saturn"), "Jupiter"),
        QuizQuestion("What color do you get by mixing red and white?", listOf("Pink", "Orange", "Purple", "Brown"), "Pink"),
        QuizQuestion("What is the freezing point of water?", listOf("0°C", "32°C", "100°C", "212°F"), "0°C"),
        QuizQuestion("What is the tallest mountain?", listOf("K2", "Kilimanjaro", "Everest", "Denali"), "Everest")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizappHWTheme {
                AppNavHost()
            }
        }
    }

    @Composable
    fun AppNavHost() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") { SplashScreen(navController) }
            composable("quiz") { QuizScreen(navController) }
            composable("stats") {
                StatsScreen(score = score, onPlayAgain = {
                    resetQuiz(navController)
                })
            }
        }
    }

    @Composable
    fun SplashScreen(navController: NavController) {
        LaunchedEffect(Unit) {
            delay(3000)
            navController.navigate("quiz")
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Welcome to Who Wants to Be a Millionaire", fontSize = 24.sp)
        }
    }

    @Composable
    fun QuizScreen(navController: NavController) {
        val orientation = LocalConfiguration.current.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ColumnQuizUI(navController)
        } else {
            RowQuizUI(navController)
        }
    }

    @Composable
    fun ColumnQuizUI(navController: NavController) {
        var selectedAnswer by remember { mutableStateOf<String?>(null) }

        // Get the current question
        val currentQuestion = questions[currentQuestionIndex]
        val context = LocalContext.current

        QuestionScreen(
            question = currentQuestion.question,
            answers = currentQuestion.answers,
            correctAnswer = currentQuestion.correctAnswer,
            selectedAnswer = selectedAnswer,
            onAnswerSelected = { selectedAnswer = it },
            onConfirm = {
                if (selectedAnswer == currentQuestion.correctAnswer) {
                    score += 100
                    Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Wrong Answer", Toast.LENGTH_SHORT).show()
                }

                if (currentQuestionIndex < questions.lastIndex) {
                    currentQuestionIndex++ // Move to the next question
                    selectedAnswer = null // Reset selected answer
                } else {
                    // Navigate to stats screen and pass the score
                    navController.currentBackStackEntry?.arguments?.putInt("score", score)
                    navController.navigate("stats")
                }
            },
            score = score
        )
    }

    @Composable
    fun RowQuizUI(navController: NavController) {
        ColumnQuizUI(navController) // You can add custom layout for landscape mode here
    }

    @Composable
    fun QuestionScreen(
        question: String,
        answers: List<String>,
        correctAnswer: String,
        selectedAnswer: String?,
        onAnswerSelected: (String) -> Unit,
        onConfirm: () -> Unit,
        score: Int
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = question,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Column {
                answers.forEach { answer ->
                    Button(
                        onClick = { onAnswerSelected(answer) },
                        colors = ButtonDefaults.buttonColors(
                            if (selectedAnswer == answer) Color.Green else Color.Gray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = answer)
                    }
                }
            }

            Button(
                onClick = { onConfirm() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Confirm")
            }

            Text(
                text = "Current Score: $$score",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    @Composable
    fun StatsScreen(score: Int, onPlayAgain: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Your Final Score: $$score", fontSize = 24.sp)
            Button(onClick = { onPlayAgain() }) {
                Text(text = "Play Again")
            }
        }
    }

    // Reset the quiz state
    private fun resetQuiz(navController: NavController) {
        score = 0
        currentQuestionIndex = 0
        navController.navigate("quiz") {
            // Clear back stack to prevent going back to stats
            popUpTo("quiz") { inclusive = true }
        }
    }
}
