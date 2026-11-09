package com.enssat.kanaquiz.mgaillar_adely.data

import android.content.Context
import com.enssat.kanaquiz.mgaillar_adely.data.db.QuizDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class QuizResultRepository private constructor(context: Context) {
    private val dao = QuizDatabase.getInstance(context).quizResultDao()
    private val mutex = Mutex()
    private val mutableResults = MutableStateFlow<List<QuizResult>>(emptyList())
    val results = mutableResults.asStateFlow()

    suspend fun refresh() {
        mutex.withLock { mutableResults.value = dao.getAll() }
    }

    suspend fun save(result: QuizResult) {
        mutex.withLock {
            dao.insertAll(listOf(result))
            mutableResults.value = dao.getAll()
        }
    }

    companion object {
        @Volatile private var instance: QuizResultRepository? = null
        fun getInstance(context: Context): QuizResultRepository = instance ?: synchronized(this) {
            instance ?: QuizResultRepository(context.applicationContext).also { instance = it }
        }
    }
}
