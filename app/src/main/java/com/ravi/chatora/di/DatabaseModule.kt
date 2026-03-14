package com.ravi.chatora.di

import android.content.Context
import androidx.room.Room
import com.ravi.chatora.data.local.ChatoraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideChatoraDatabase(
        @ApplicationContext context: Context
    ): ChatoraDatabase = Room.databaseBuilder(
        context = context,
        klass = ChatoraDatabase::class.java,
        name = "chatora_db"
    ).build()

}