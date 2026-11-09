package com.enssat.kanaquiz.mgaillar_adely.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import com.enssat.kanaquiz.mgaillar_adely.data.*
import com.enssat.kanaquiz.mgaillar_adely.tts.JapaneseSpeechService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class Page { HOME, LEARNING, SETUP, QUIZ, RESULT, PROGRESS }

data class QuizUiState(
    val page: Page = Page.HOME,
    val progressType: CharacterType = CharacterType.HIRAGANA,
    val params: QuizParams = QuizParams(CharacterType.HIRAGANA, QuestionType.CHARACTER_TO_ROMAJI),
    val session: QuizSession? = null,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    val historyError: String? = null,
    val confirmExit: Boolean = false,
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuizResultRepository.getInstance(application)
    private val speech = JapaneseSpeechService(application)
    private val mutableState = MutableStateFlow(QuizUiState())
    val state = mutableState.asStateFlow()
    val results = repository.results
    private var generator: QuizGenerator? = null
    private var pendingResult: QuizResult? = null
    private var startJob: Job? = null

    init { refreshHistory() }

    fun refreshHistory() {
        viewModelScope.launch {
            try {
                repository.refresh()
                mutableState.update { it.copy(historyError = null) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                mutableState.update { it.copy(historyError = "Impossible de charger la progression.") }
            }
        }
    }

    fun navigate(page: Page) {
        speech.stop()
        mutableState.update { it.copy(page = page, error = null) }
    }

    fun showProgress(type: CharacterType) {
        speech.stop()
        mutableState.update { it.copy(page = Page.PROGRESS, progressType = type, error = null) }
    }

    fun back() {
        if (state.value.loading || state.value.saving) return
        if (state.value.page == Page.QUIZ) {
            mutableState.update { it.copy(confirmExit = true) }
        } else navigate(Page.HOME)
    }

    fun dismissExit() { mutableState.update { it.copy(confirmExit = false) } }

    fun abandon() {
        speech.stop()
        startJob?.cancel()
        pendingResult = null
        mutableState.update { it.copy(page = Page.HOME, session = null, confirmExit = false, error = null) }
    }

    fun setParams(params: QuizParams) {
        if (state.value.loading) return
        val question = params.questionType.takeIf { it.supports(params.characterType) }
            ?: QuestionType.entries.first { it.supports(params.characterType) }
        mutableState.update { it.copy(params = params.copy(questionType = question)) }
    }

    fun start() {
        if (state.value.loading || state.value.saving) return
        speech.stop()
        val params = state.value.params
        pendingResult = null
        mutableState.update { it.copy(loading = true, error = null, saved = false) }
        startJob = viewModelScope.launch {
            try {
                val quiz = withContext(Dispatchers.IO) {
                    val source = generator ?: KanaRepository().let {
                        QuizGenerator(it.loadKanas(getApplication()), it.loadKanjis(getApplication()))
                    }.also { generator = it }
                    source.generate(params)
                }
                mutableState.update { it.copy(page = Page.QUIZ, session = QuizSession(quiz), loading = false) }
                speakQuestion()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                mutableState.update { it.copy(loading = false, error = "Impossible de préparer le quiz. Réessaie.") }
            }
        }
    }

    fun answer(choice: Int) {
        val session = state.value.session ?: return
        if (state.value.page != Page.QUIZ || session.answered) return
        val answered = session.answer(choice)
        if (answered == session) return
        mutableState.update { it.copy(session = answered) }
        session.question.speechAfterAnswer?.let { speech.speak(it, alternateSpeed = false) }
    }

    fun next() {
        val session = state.value.session ?: return
        if (!session.answered || state.value.saving || state.value.page != Page.QUIZ) return
        if (session.complete) {
            pendingResult = pendingResult ?: session.result(System.currentTimeMillis())
            save()
        } else {
            speech.stop()
            mutableState.update { it.copy(session = session.advance()) }
            speakQuestion()
        }
    }

    private fun speakQuestion() {
        state.value.session?.question?.speechBeforeAnswer?.let { speech.speak(it, alternateSpeed = false) }
    }

    fun replaySpeech() {
        val session = state.value.session ?: return
        val text = if (session.answered) session.question.speechAfterAnswer else session.question.speechBeforeAnswer
        text?.let { speech.speak(it, alternateSpeed = false) }
    }

    private fun save() {
        val result = pendingResult ?: return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                repository.save(result)
                speech.stop()
                mutableState.update { it.copy(page = Page.RESULT, saving = false, saved = true, historyError = null) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                mutableState.update { it.copy(saving = false, error = "Enregistrement impossible. Réessaie sans quitter le quiz.") }
            }
        }
    }

    override fun onCleared() { speech.shutdown() }
}
