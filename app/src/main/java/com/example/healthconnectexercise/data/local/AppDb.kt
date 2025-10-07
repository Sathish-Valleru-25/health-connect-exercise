package com.example.healthconnectexercise.data.local


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExerciseEntity::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}
