package com.ravi.chatora.presentation.chatora.components

import androidx.lifecycle.ViewModel
import com.ravi.chatora.domain.models.VoiceToTextParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class VoiceChatoraViewModel @Inject constructor(
    private val voiceToTextParser: VoiceToTextParser
) : ViewModel() {

    val voiceState = voiceToTextParser.voiceState

    fun startVoiceChatora(){
        voiceToTextParser.startListening()
    }

    fun stopVoiceChatora(){
        voiceToTextParser.stopListening()
    }


}