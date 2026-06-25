package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OtpVerificationScreen
import com.example.ui.screens.SignupScreen
import com.example.ui.screens.TuitionMainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TuitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge drawing under status and navigation bars
        enableEdgeToEdge()

        setContent {
            // Dark theme state management
            val systemDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDarkTheme) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }
                    ) { screen ->
                        when (screen) {
                            Screen.Login -> LoginScreen(viewModel = viewModel)
                            Screen.Signup -> SignupScreen(viewModel = viewModel)
                            Screen.ForgotPassword -> ForgotPasswordScreen(viewModel = viewModel)
                            Screen.OtpVerification -> OtpVerificationScreen(viewModel = viewModel)
                            else -> TuitionMainScreen(
                                viewModel = viewModel,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }
                    }
                }
            }
        }
    }
}
