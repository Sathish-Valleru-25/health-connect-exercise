package com.example.healthconnectexercise.data.local

import androidx.room.*

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY start DESC")
    suspend fun getAll(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ExerciseEntity)

    @Delete
    suspend fun delete(log: ExerciseEntity)
}
