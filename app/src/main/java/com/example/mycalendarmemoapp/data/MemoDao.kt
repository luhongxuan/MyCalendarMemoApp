package com.example.mycalendarmemoapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {

    @Query("SELECT * FROM memos WHERE date = :date ORDER BY time ASC")
    fun getMemosForDate(date: Long): Flow<List<Memo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: Memo): Long

    @Delete
    suspend fun deleteMemo(memo: Memo)

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getMemoById(id: Int): Memo?
}