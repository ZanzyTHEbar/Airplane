package com.example.android.wearable.composeforwearos.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// TODO: Handle the data for a trends view

class TrendsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    // FIXME: change to yse custom data type for all the data
    private var temperature: String? = savedStateHandle.get<String>("data")


    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            try {

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}