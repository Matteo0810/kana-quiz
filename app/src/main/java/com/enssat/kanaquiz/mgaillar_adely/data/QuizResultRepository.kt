package com.enssat.kanaquiz.mgaillar_adely.data

import android.content.Context
import com.enssat.kanaquiz.mgaillar_adely.data.db.QuizDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class QuizResultRepository private constructor(context: Context) {
    private val database = QuizDatabase(context)
    private val mutex = Mutex()
    private val mutableResults = MutableStateFlow<List<QuizResult>>(emptyList())
    val results = mutableResults.asStateFlow()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        mutex.withLock { mutableResults.value = database.readAll() }
    }

    suspend fun save(result: QuizResult) = withContext(Dispatchers.IO) {
        mutex.withLock {
            database.insert(result)
            mutableResults.value = database.readAll()
        }
    }

    companion object {
        @Volatile private var instance: QuizResultRepository? = null
        fun getInstance(context: Context): QuizResultRepository = instance ?: synchronized(this) {
            instance ?: QuizResultRepository(context.applicationContext).also { instance = it }
        }
    }
}
