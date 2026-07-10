package com.companion.learning.di

import android.content.Context
import com.companion.learning.data.parser.DocumentParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides
    @Singleton
    fun provideDocumentParser(@ApplicationContext context: Context): DocumentParser {
        return DocumentParser(context)
    }
}
