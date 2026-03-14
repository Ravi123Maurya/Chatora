package com.ravi.chatora.presentation.chatora

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.domain.repository.ChatoraRepository
import com.ravi.chatora.utils.formatTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatoraViewModel @Inject constructor(
    private val chatoraRepository: ChatoraRepository
) : ViewModel() {


    private val _getChatoraResponseState = MutableStateFlow<GetChatoraResponse>(GetChatoraResponse.Idle)
    val getChatoraResponseState = _getChatoraResponseState.asStateFlow()

    private val _chatoras = MutableStateFlow<List<Chatora>>(emptyList())
    val chatoras = _chatoras.asStateFlow()

    private val _chatUiState = MutableStateFlow<List<Chatora>>(emptyList())
    val chatUiState: StateFlow<List<Chatora>> = _chatUiState.asStateFlow()


    init {
        getAllChatoras()
        viewModelScope.launch {
            // This combines the latest values from both flows
            combine(chatoras, getChatoraResponseState) { dbMessages, responseState ->
                val currentList = dbMessages.toMutableList()

                // If the API is loading, add a temporary "Loading" message
                if (responseState is GetChatoraResponse.Loading) {
                    currentList.add(Chatora(message = "Generating...", isUser = false, timeStamp = ""))
                }

                // If the API succeeded, the new messages will already be in the database
                // and `dbMessages` will update automatically, so no special handling is needed here.

                currentList // Emit the final combined list
            }.collect { combinedList ->
                _chatUiState.value = combinedList
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getChatoraResponse(message: String) {
        viewModelScope.launch {

            val userMessage = Chatora(
                message = message,
                isUser = true,
                timeStamp = formatTime(System.currentTimeMillis())
            )
            addChatora(userMessage) // Add user chatora to database

            _getChatoraResponseState.value = GetChatoraResponse.Loading


            /* ----------------------------------------------------------------- */


            try {
                val response = chatoraRepository.getQueryResponse(message, _chatoras.value)

                val responseChatora = Chatora(
                    message = response,
                    isUser = false,
                    timeStamp = formatTime(System.currentTimeMillis())
                )

//              _chatoras.value = _chatoras.value + responseChatora // Add response chatora
                _getChatoraResponseState.value = GetChatoraResponse.Idle
                addChatora(responseChatora) // Add response chatora to database

            } catch (e: Exception) {
                val updatedChatoras = _chatoras.value.toMutableList()
                updatedChatoras.removeAt(updatedChatoras.size - 1)
                val errorChatora = Chatora(
                    message = "Something went wrong",
                    isUser = false,
                    timeStamp = formatTime(System.currentTimeMillis())
                )
                _getChatoraResponseState.value = GetChatoraResponse.Idle
                updatedChatoras.add(errorChatora)
                _chatoras.value = updatedChatoras
                Log.d("ChatoraViewModel", "Error removing Loading - $e")
            }

        }
    }


    private fun getAllChatoras(){
        viewModelScope.launch(Dispatchers.IO) {
            chatoraRepository.getAllChatoras().collect {
                _chatoras.value = it
            }
        }
    }

    private fun addChatora(chatora: Chatora){
        viewModelScope.launch(Dispatchers.IO) {
            chatoraRepository.addChatora(chatora)
        }
    }


    fun deleterChatora(id: Int){
        viewModelScope.launch(Dispatchers.IO) {
            chatoraRepository.deleteChatora(id)
        }
    }
}

sealed class GetChatoraResponse{
    object Idle : GetChatoraResponse()
    object Loading : GetChatoraResponse()
}