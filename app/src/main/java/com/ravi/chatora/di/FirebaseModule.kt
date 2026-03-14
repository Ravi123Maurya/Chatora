package com.ravi.chatora.di

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.ravi.chatora.utils.ApiConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAi() : GenerativeModel{
        return Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(ApiConstants.GEMINI_FLASH)
    }

    @Provides
    @Singleton
    fun provideFirebase() : Firebase{
        return Firebase
    }

}