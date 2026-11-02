package com.jarvis.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import com.jarvis.assistant.ui.chat.ChatScreen
import com.jarvis.assistant.ui.dashboard.DashboardScreen
import com.jarvis.assistant.ui.theme.BgDeep

sealed class Screen { object Dashboard; object Chat }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
            Box(Modifier.fillMaxSize().background(BgDeep)) {
                when (screen) {
                    Screen.Dashboard -> DashboardScreen(
                        onTalk = { screen = Screen.Chat },
                        onAgents = { screen = Screen.Chat }
                    )
                    Screen.Chat -> ChatScreen(onBack = { screen = Screen.Dashboard })
                }
            }
        }
    }
}
