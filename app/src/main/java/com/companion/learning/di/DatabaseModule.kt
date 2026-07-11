package com.companion.learning.di

import android.content.Context
import androidx.room.Room
import com.companion.learning.data.local.LearningDatabase
import com.companion.learning.data.local.dao.RoadmapDao
import com.companion.learning.data.local.dao.CurriculumDao
import com.companion.learning.data.local.dao.UserDao
import com.companion.learning.data.local.dao.NoteDao
import com.companion.learning.data.local.dao.QuizDao
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
    fun provideLearningDatabase(@ApplicationContext context: Context): LearningDatabase {
        return Room.databaseBuilder(
            context,
            LearningDatabase::class.java,
            "learning_companion_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRoadmapDao(database: LearningDatabase): RoadmapDao {
        return database.roadmapDao
    }

    @Provides
    fun provideCurriculumDao(database: LearningDatabase): CurriculumDao {
        return database.curriculumDao
    }

    @Provides
    fun provideUserDao(database: LearningDatabase): UserDao {
        return database.userDao
    }

    @Provides
    fun provideNoteDao(database: LearningDatabase): NoteDao {
        return database.noteDao
    }

    @Provides
    fun provideQuizDao(database: LearningDatabase): QuizDao {
        return database.quizDao
    }
}
