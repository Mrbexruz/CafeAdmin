package com.example.cafeadmin.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _selectedTableId = MutableLiveData<Int>()
    val selectedTableId: LiveData<Int> get() = _selectedTableId

    private val _selectedTableKey = MutableLiveData<String>()
    val selectedTableKey: LiveData<String> get() = _selectedTableKey

    fun setSelectedTable(id: Int, key: String) {
        _selectedTableId.value = id
        _selectedTableKey.value = key
    }
}
