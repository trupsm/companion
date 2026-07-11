package com.companion.learning.di

import com.companion.learning.data.repository.RoadmapRepositoryImpl
import com.companion.learning.domain.repository.RoadmapRepository
import com.companion.learning.data.repository.CurriculumRepositoryImpl
import com.companion.learning.domain.repository.CurriculumRepository
import com.companion.learning.data.repository.QuizRepositoryImpl
import com.companion.learning.domain.repository.QuizRepository
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
    abstract fun bindRoadmapRepository(
        roadmapRepositoryImpl: RoadmapRepositoryImpl
    ): RoadmapRepository

    @Binds
    @Singleton
    abstract fun bindCurriculumRepository(
        curriculumRepositoryImpl: CurriculumRepositoryImpl
    ): CurriculumRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        quizRepositoryImpl: QuizRepositoryImpl
    ): QuizRepository
}
