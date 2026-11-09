
package com.enssat.kanaquiz.mgaillar_adely.ui.learning

enum class CharacterType {
    HIRAGANA,
    KATAKANA,
    KANJI
}

data class LearningCharacter(
    val character: String,
    val pronunciation: String,
    val category: String,
    val type: CharacterType,
    val description: String? = null
)

data class LearningUiState(
    val characters: List<LearningCharacter> = emptyList(),
    val selectedType: CharacterType = CharacterType.HIRAGANA,
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filteredCharacters: List<LearningCharacter>
        get() = characters.filter { it.type == selectedType }

    val currentCharacter: LearningCharacter?
        get() = filteredCharacters.getOrNull(currentIndex)

    val hasPrevious: Boolean
        get() = currentIndex > 0

    val hasNext: Boolean
        get() = currentIndex < filteredCharacters.lastIndex

    val totalCharacters: Int
        get() = filteredCharacters.size
}
