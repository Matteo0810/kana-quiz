package com.enssat.kanaquiz.mgaillar_adely.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult

@Dao
interface QuizResultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(results: List<QuizResult>)

    @Query("SELECT * FROM quiz_results ORDER BY timestamp ASC, id ASC")
    suspend fun getAll(): List<QuizResult>

    @Query("SELECT * FROM quiz_results WHERE isFake = 1 ORDER BY timestamp ASC, id ASC")
    suspend fun getFake(): List<QuizResult>

    @Query("DELETE FROM quiz_results WHERE isFake = 1")
    suspend fun deleteFake()

    @Query("DELETE FROM quiz_results WHERE id = :id")
    suspend fun deleteById(id: String)
}
