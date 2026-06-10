package com.example.womensafety.presentation.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.womensafety.domain.model.SosRecord
import com.example.womensafety.domain.repository.SosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SosViewModel @Inject constructor(
    private val sosRepository: SosRepository
) : ViewModel() {

    val sosHistory: StateFlow<List<SosRecord>> = sosRepository.getAllSosRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
