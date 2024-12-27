package com.exner.tools.activitytimercompanion.data.di

import android.content.Context
import androidx.room.Room
import com.exner.tools.activitytimercompanion.data.persistence.TimerDataDAO
import com.exner.tools.activitytimercompanion.data.persistence.TimerDataRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppComponent {

    @Singleton
    @Provides
    fun provideDao(ftDatabase: TimerDataRoomDatabase): TimerDataDAO =
        ftDatabase.processDAO()

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        provider: Provider<TimerDataDAO>
    ): TimerDataRoomDatabase =
        Room.inMemoryDatabaseBuilder(
            context.applicationContext,
            TimerDataRoomDatabase::class.java
        ).build()
}