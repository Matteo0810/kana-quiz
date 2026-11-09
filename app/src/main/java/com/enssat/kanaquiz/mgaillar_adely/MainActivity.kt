package com.enssat.kanaquiz.mgaillar_adely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.enssat.kanaquiz.mgaillar_adely.ui.KanaQuizApp
import com.enssat.kanaquiz.mgaillar_adely.ui.theme.KanaQuizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KanaQuizTheme { KanaQuizApp() }
        }
    }
}
