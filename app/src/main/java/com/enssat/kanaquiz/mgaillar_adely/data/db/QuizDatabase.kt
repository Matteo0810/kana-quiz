package com.enssat.kanaquiz.mgaillar_adely.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult

@Database(entities = [QuizResult::class], version = 1, exportSchema = false)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun quizResultDao(): QuizResultDao

    companion object {
        private const val NAME = "kanaquiz_room.db"

        @Volatile private var instance: QuizDatabase? = null

        fun getInstance(context: Context): QuizDatabase = instance ?: synchronized(this) {
            instance ?: create(context, NAME).also { instance = it }
        }

        fun create(context: Context, name: String): QuizDatabase =
            Room.databaseBuilder(context.applicationContext, QuizDatabase::class.java, name).build()
    }
}
