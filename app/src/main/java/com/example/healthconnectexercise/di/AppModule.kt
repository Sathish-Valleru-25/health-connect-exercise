package com.example.healthconnectexercise.di

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.room.Room
import com.example.healthconnectexercise.data.local.AppDb
import com.example.healthconnectexercise.data.local.ExerciseDao
import com.example.healthconnectexercise.domain.repository.ExerciseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDb {
        return Room.databaseBuilder(
                context,
                AppDb::class.java,
                "exercise_db"
            ).fallbackToDestructiveMigration(false).build()
    }

    @Provides
    fun provideExerciseDao(appDb: AppDb): ExerciseDao = appDb.exerciseDao()

    @Provides
    @Singleton
    fun provideHealthConnectClient(@ApplicationContext context: Context): HealthConnectClient {
        return HealthConnectClient.getOrCreate(context)
    }

}
