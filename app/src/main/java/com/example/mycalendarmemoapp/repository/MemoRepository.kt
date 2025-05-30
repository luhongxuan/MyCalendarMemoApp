package com.example.mycalendarmemoapp.repository

import com.example.mycalendarmemoapp.data.Memo
import com.example.mycalendarmemoapp.data.MemoDao
import kotlinx.coroutines.flow.Flow

class MemoRepository(private val memoDao: MemoDao) {

    fun getMemosForDate(date: Long): Flow<List<Memo>> {
        return memoDao.getMemosForDate(date)
    }

    suspend fun insertMemo(memo: Memo): Long {
        return memoDao.insertMemo(memo)
    }

    suspend fun deleteMemo(memo: Memo) {
        memoDao.deleteMemo(memo)
    }
}