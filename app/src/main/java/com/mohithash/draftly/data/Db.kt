package com.mohithash.draftly.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "drafts")
data class Draft(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val intent: String,
    val tone: String,
    val input: String,
    val output: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface DraftDao {
    @Query("SELECT * FROM drafts ORDER BY createdAt DESC LIMIT 200") fun all(): Flow<List<Draft>>
    @Insert suspend fun insert(d: Draft)
    @Query("DELETE FROM drafts WHERE id = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM drafts") suspend fun clear()
}

@Database(entities = [Draft::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() { abstract fun drafts(): DraftDao }
