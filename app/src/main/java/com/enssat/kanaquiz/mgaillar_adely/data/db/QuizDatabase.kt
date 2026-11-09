package com.enssat.kanaquiz.mgaillar_adely.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.CharacterType
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.QuestionType
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult

class QuizDatabase(context: Context, name: String = "kanaquiz.db") :
    SQLiteOpenHelper(context.applicationContext, name, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE quiz_results (
                id TEXT PRIMARY KEY NOT NULL,
                timestamp INTEGER NOT NULL,
                char_type TEXT NOT NULL,
                question_type TEXT NOT NULL,
                quiz_grade INTEGER NOT NULL,
                with_help INTEGER NOT NULL CHECK(with_help IN (0, 1)),
                score INTEGER NOT NULL CHECK(score BETWEEN 0 AND 10),
                is_fake INTEGER NOT NULL DEFAULT 0 CHECK(is_fake IN (0, 1))
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX quiz_results_date ON quiz_results(timestamp, id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("Migration SQLite manquante : $oldVersion → $newVersion")
    }

    fun insert(result: QuizResult) = insertAll(listOf(result))

    fun insertAll(results: List<QuizResult>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            results.forEach { result ->
                val values = ContentValues().apply {
                    put("id", result.id)
                    put("timestamp", result.timestamp)
                    put("char_type", result.characterType.name)
                    put("question_type", result.questionType.name)
                    put("quiz_grade", result.quizGrade)
                    put("with_help", if (result.withHelp) 1 else 0)
                    put("score", result.score)
                    put("is_fake", if (result.isFake) 1 else 0)
                }
                if (db.insertWithOnConflict("quiz_results", null, values, SQLiteDatabase.CONFLICT_IGNORE) == -1L) {
                    db.query("quiz_results", arrayOf("id"), "id = ?", arrayOf(result.id), null, null, null).use {
                        check(it.moveToFirst()) { "Impossible d’enregistrer le résultat" }
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun readAll(fakeOnly: Boolean = false): List<QuizResult> = readableDatabase.query(
        "quiz_results", null, if (fakeOnly) "is_fake = 1" else null,
        null, null, null, "timestamp ASC, id ASC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                fun text(name: String) = cursor.getString(cursor.getColumnIndexOrThrow(name))
                fun number(name: String) = cursor.getInt(cursor.getColumnIndexOrThrow(name))
                add(QuizResult(text("id"), cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                    CharacterType.valueOf(text("char_type")), QuestionType.valueOf(text("question_type")),
                    number("quiz_grade"), number("with_help") == 1, number("score"), number("is_fake") == 1))
            }
        }
    }

    fun deleteFake() { writableDatabase.delete("quiz_results", "is_fake = 1", null) }
}
