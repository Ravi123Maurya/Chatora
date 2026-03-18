package com.ravi.chatora.presentation.chatorahistory

import androidx.lifecycle.ViewModel
import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.domain.repository.ChatoraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


data class UiState(
    val history: List<Chatora> = emptyList()
)

sealed class HistoryEvent{
    data class OnHistoryClick(val chatora: Chatora): HistoryEvent()
}

@HiltViewModel
class ChatoraHistoryViewModel @Inject constructor(
    private val chatoraRepository: ChatoraRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()


    init {

    }

    fun onEvent(event: HistoryEvent){
        when(event){
            is HistoryEvent.OnHistoryClick -> {}
        }
    }

}
