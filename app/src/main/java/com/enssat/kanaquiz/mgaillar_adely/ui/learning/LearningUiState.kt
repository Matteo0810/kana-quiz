
package com.enssat.kanaquiz.mgaillar_adely.ui.learning

data class LearningCharacter(
    val character: String,
    val pronunciation: String,
    val category: String,
    val description: String? = null
)

data class LearningUiState(
    val characters: List<LearningCharacter> = emptyList(),
    val currentIndex: Int = 0
) {
    val currentCharacter: LearningCharacter?
        get() = characters.getOrNull(currentIndex)

    val hasPrevious: Boolean
        get() = currentIndex > 0

    val hasNext: Boolean
        get() = currentIndex < characters.lastIndex
}
