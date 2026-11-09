
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

class LearningViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = KanaRepository()

    private val _uiState = MutableStateFlow(LearningUiState())

    val uiState: StateFlow<LearningUiState> =
        _uiState.asStateFlow()

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            try {
                val characters = withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()

                    repository.loadKanas(context).map {
                        it.toLearningCharacter()
                    } + repository.loadKanjis(context).map {
                        it.toLearningCharacter()
                    }
                }

                _uiState.update {
                    it.copy(
                        characters = characters,
                        currentIndex = 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur de chargement"
                    )
                }
            }
        }
    }

    fun selectCategory(type: CharacterType) {
        _uiState.update {
            it.copy(
                selectedType = type,
                currentIndex = 0
            )
        }
    }

    fun nextCharacter() {
        _uiState.update { state ->
            if (state.hasNext) {
                state.copy(
                    currentIndex = state.currentIndex + 1
                )
            } else {
                state
            }
        }
    }

    fun previousCharacter() {
        _uiState.update { state ->
            if (state.hasPrevious) {
                state.copy(
                    currentIndex = state.currentIndex - 1
                )
            } else {
                state
            }
        }
    }
}

private fun Kana.toLearningCharacter(): LearningCharacter {

    val type = when (char.firstOrNull()) {
        in 'ぁ'..'ゟ' -> CharacterType.HIRAGANA
        in 'ァ'..'ヿ' -> CharacterType.KATAKANA
        else -> error("Caractère kana inconnu : $char")
    }

    return LearningCharacter(
        character = char,
        pronunciation = romaji,
        category = when (type) {
            CharacterType.HIRAGANA -> "Hiragana"
            CharacterType.KATAKANA -> "Katakana"
            CharacterType.KANJI -> "Kanji"
        },
        type = type
    )
}

private fun Kanji.toLearningCharacter() = LearningCharacter(
    character = char,
    pronunciation = (readingsOn + readingsKun).joinToString("、"),
    category = "Kanji · Grade $grade",
    type = CharacterType.KANJI,
    description = meanings.joinToString(", ")
)
