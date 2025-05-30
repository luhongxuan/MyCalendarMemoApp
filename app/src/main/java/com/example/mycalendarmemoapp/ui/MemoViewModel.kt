package com.example.mycalendarmemoapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.mycalendarmemoapp.data.AppDatabase
import com.example.mycalendarmemoapp.data.Memo
import com.example.mycalendarmemoapp.repository.MemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class MemoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MemoRepository
    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    val memosForCurrentDate: LiveData<List<Memo>>

    init {
        val memoDao = AppDatabase.getDatabase(application).memoDao()
        repository = MemoRepository(memoDao)

        // 使用 LiveData 來觀察資料庫變化
        memosForCurrentDate = currentDate.asLiveData().switchMap { date -> // 直接在 LiveData 物件上調用 .switchMap
            repository.getMemosForDate(
                date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ).asLiveData()
        }
    }

    fun addMemo(time: String, title: String, location: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val memo = Memo(
                date = _currentDate.value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                time = time,
                title = title,
                location = location?.takeIf { it.isNotBlank() }
            )
            repository.insertMemo(memo)
            // 可以在這裡加入設定提醒通知的邏輯 [cite: 3]
        }
    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMemo(memo)
        }
    }

    // 你也可以加入更新日期的方法，如果 App 需要顯示其他日期的話
    // fun changeDate(newDate: LocalDate) {
    //     _currentDate.value = newDate
    // }
}

// 需要一個 ViewModel Factory 來建立 MemoViewModel
class MemoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}