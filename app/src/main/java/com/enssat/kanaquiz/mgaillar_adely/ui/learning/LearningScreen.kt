
package com.enssat.kanaquiz.mgaillar_adely.ui.learning

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import com.enssat.kanaquiz.mgaillar_adely.tts.JapaneseSpeechService
import com.enssat.kanaquiz.mgaillar_adely.ui.components.CharacterCard

@Composable
fun LearningScreen(
    modifier: Modifier = Modifier,
    learningViewModel: LearningViewModel = viewModel()
) {

    val uiState by learningViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val speechService = remember(context) {
        JapaneseSpeechService(context)
    }

    DisposableEffect(speechService) {
        onDispose {
            speechService.shutdown()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = "KanaQuiz",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = if (uiState.characters.isEmpty()) {
                "1 / 0"
            } else {
                "${uiState.currentIndex + 1} / ${uiState.characters.size}"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        // Carte du caractère courant
        uiState.currentCharacter?.let { current ->
            CharacterCard(
                character = current.character,
                pronunciation = current.pronunciation,
                category = current.category,
                description = current.description,
                onSpeakClick = {
                    speechService.speak(current.character)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedButton(
                onClick = {
                    learningViewModel.previousCharacter()
                },
                enabled = uiState.hasPrevious,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Précédent"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Précédent")
            }

            Button(
                onClick = {
                    learningViewModel.nextCharacter()
                },
                enabled = uiState.hasNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Suivant")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Suivant"
                )
            }
        }
    }
}
