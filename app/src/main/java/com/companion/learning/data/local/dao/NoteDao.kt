package com.companion.learning.data.local.dao

import androidx.room.*
import com.companion.learning.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE dayDate = :dayDate ORDER BY createdAt DESC")
    fun getNotesForDate(dayDate: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE curriculumItemId = :itemId ORDER BY createdAt DESC")
    fun getNotesForTopic(itemId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE roadmapId = :roadmapId ORDER BY createdAt DESC")
    fun getNotesForRoadmap(roadmapId: String): Flow<List<NoteEntity>>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    @Query("UPDATE notes SET title = :title, content = :content, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNote(id: String, title: String, content: String, updatedAt: Long)
}
