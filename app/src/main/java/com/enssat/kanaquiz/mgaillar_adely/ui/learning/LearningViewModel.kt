
package com.enssat.kanaquiz.mgaillar_adely.ui.learning

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LearningViewModel : ViewModel() {

    private val sampleCharacters = listOf(
        LearningCharacter("あ", "a", "Hiragana", "Voyelle"),
        LearningCharacter("い", "i", "Hiragana", "Voyelle"),
        LearningCharacter("う", "u", "Hiragana", "Voyelle"),
        LearningCharacter("ア", "a", "Katakana", "Voyelle"),
        LearningCharacter("山", "やま", "Kanji", "Mountain")
    )

    private val _uiState = MutableStateFlow(
        LearningUiState(characters = sampleCharacters)
    )

    val uiState: StateFlow<LearningUiState> =
        _uiState.asStateFlow()

    fun nextCharacter() {
        _uiState.update { state ->
            if (state.hasNext) {
                state.copy(currentIndex = state.currentIndex + 1)
            } else {
                state
            }
        }
    }

    fun previousCharacter() {
        _uiState.update { state ->
            if (state.hasPrevious) {
                state.copy(currentIndex = state.currentIndex - 1)
            } else {
                state
            }
        }
    }
}
