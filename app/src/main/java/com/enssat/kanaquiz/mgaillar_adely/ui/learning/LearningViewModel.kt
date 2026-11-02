package com.enssat.kanaquiz.mgaillar_adely.ui.learning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enssat.kanaquiz.mgaillar_adely.data.Kana
import com.enssat.kanaquiz.mgaillar_adely.data.KanaRepository
import com.enssat.kanaquiz.mgaillar_adely.data.Kanji
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// AndroidViewModel (au lieu de ViewModel) pour avoir accès au Context
// de l'application, nécessaire pour lire les fichiers de res/raw.
class LearningViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KanaRepository()

    private val _uiState = MutableStateFlow(LearningUiState())

    val uiState: StateFlow<LearningUiState> =
        _uiState.asStateFlow()

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            // Lecture + parsing des JSON hors du thread UI
            val characters = withContext(Dispatchers.IO) {
                val context = getApplication<Application>()
                repository.loadKanas(context).map { it.toLearningCharacter() } +
                    repository.loadKanjis(context).map { it.toLearningCharacter() }
            }
            _uiState.update { it.copy(characters = characters) }
        }
    }

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

// Les hiragana sont dans le bloc Unicode U+3040..U+309F, les katakana juste après.
private fun Kana.toLearningCharacter() = LearningCharacter(
    character = char,
    pronunciation = romaji,
    category = if (char.first() in '぀'..'ゟ') "Hiragana" else "Katakana",
)

private fun Kanji.toLearningCharacter() = LearningCharacter(
    character = char,
    pronunciation = (readingsOn + readingsKun).joinToString("、"),
    category = "Kanji · Grade $grade",
    description = meanings.joinToString(", "),
)
