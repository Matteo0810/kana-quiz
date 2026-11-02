package com.enssat.kanaquiz.mgaillar_adely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.enssat.kanaquiz.mgaillar_adely.ui.CharacterScreen
import com.enssat.kanaquiz.mgaillar_adely.ui.theme.KanaQuizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KanaQuizTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CharacterScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
