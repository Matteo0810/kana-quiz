
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
    val uiState by learningViewModel.uiState
        .collectAsStateWithLifecycle()

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CharacterType.entries.forEach { type ->

                val label = when (type) {
                    CharacterType.HIRAGANA -> "Hiragana"
                    CharacterType.KATAKANA -> "Katakana"
                    CharacterType.KANJI -> "Kanji"
                }

                FilterChip(
                    selected = uiState.selectedType == type,
                    onClick = {
                        learningViewModel.selectCategory(type)
                    },
                    label = {
                        Text(label)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text(
            text = if (uiState.totalCharacters == 0) {
                "0 / 0"
            } else {
                "${uiState.currentIndex + 1} / ${uiState.totalCharacters}"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "Erreur",
                    color = MaterialTheme.colorScheme.error
                )
            }

            uiState.currentCharacter != null -> {
                val current = uiState.currentCharacter!!

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

            else -> {
                Text("Aucun caractère disponible")
            }
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
                enabled = uiState.hasPrevious && !uiState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Précédent"
                )
                Spacer(Modifier.width(8.dp))
                Text("Précédent")
            }

            Button(
                onClick = {
                    learningViewModel.nextCharacter()
                },
                enabled = uiState.hasNext && !uiState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Suivant")
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Suivant"
                )
            }
        }
    }
}
