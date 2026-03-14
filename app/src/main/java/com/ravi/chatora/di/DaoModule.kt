package com.ravi.chatora.di

import com.ravi.chatora.data.local.ChatoraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun provideChatoraDao(chatoraDatabase: ChatoraDatabase) = chatoraDatabase.chatoraDao()

}