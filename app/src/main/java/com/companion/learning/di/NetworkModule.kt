package com.companion.learning.di

import com.companion.learning.data.local.security.SecureStorage
import com.companion.learning.data.remote.GeminiProvider
import com.companion.learning.domain.provider.LlmProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideLlmProvider(
        secureStorage: SecureStorage,
        client: OkHttpClient
    ): LlmProvider {
        return GeminiProvider(secureStorage, client)
    }
}
