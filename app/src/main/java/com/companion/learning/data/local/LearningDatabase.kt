package com.companion.learning.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.companion.learning.data.local.dao.RoadmapDao
import com.companion.learning.data.local.dao.CurriculumDao
import com.companion.learning.data.local.dao.UserDao
import com.companion.learning.data.local.dao.NoteDao
import com.companion.learning.data.local.dao.QuizDao
import com.companion.learning.data.local.entity.*

@Database(
    entities = [
        RoadmapEntity::class,
        MilestoneEntity::class,
        CurriculumItemEntity::class,
        NoteEntity::class,
        QuizQuestionEntity::class,
        StreakLogEntity::class,
        ReviewScheduleEntity::class,
        UserEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LearningDatabase : RoomDatabase() {
    abstract val roadmapDao: RoadmapDao
    abstract val curriculumDao: CurriculumDao
    abstract val userDao: UserDao
    abstract val noteDao: NoteDao
    abstract val quizDao: QuizDao
    abstract val streakDao: com.companion.learning.data.local.dao.StreakDao
}
