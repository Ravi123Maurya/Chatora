package com.ravi.chatora.di

import com.ravi.chatora.data.repository.ChatoraRepositoryImpl
import com.ravi.chatora.domain.repository.ChatoraRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatoraRepository(
        chatoraRepositoryImpl: ChatoraRepositoryImpl
    ) : ChatoraRepository

}