package com.ravi.chatora.domain.models

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class VoiceState(
    val isSpeaking: Boolean = false,
    val spokenText: String = "",
    val error: String? = null
)

class VoiceToTextParser(private val app: Application) : RecognitionListener{

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState())
    val voiceState = _voiceState.asStateFlow()

    private val recognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(app)

    fun startListening(languageCode: String = "en-US"){
        _voiceState.update { VoiceState() }

        if (!SpeechRecognizer.isRecognitionAvailable(app)){
            _voiceState.update {
                it.copy(error = "Recognition is not available")
            }
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        }
        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
        _voiceState.update { it.copy(isSpeaking = true) }
    }

    fun stopListening(){
        _voiceState.update { VoiceState() }
        recognizer.stopListening()
    }



    override fun onBeginningOfSpeech() = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
       _voiceState.update { it.copy(isSpeaking = false) }
    }

    override fun onError(error: Int) {
       if (error == SpeechRecognizer.ERROR_CLIENT) return
        _voiceState.update { it.copy(error = "Error: $error", isSpeaking = false) }
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    override fun onPartialResults(partialResults: Bundle?){
        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)?.let { text ->
            _voiceState.update { it.copy(spokenText = text) }
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _voiceState.update { it.copy(error = null) }
    }

    override fun onResults(results: Bundle?) {
        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)?.let { text ->
            _voiceState.update { it.copy(spokenText = text) }
        }
    }

    override fun onRmsChanged(rmsdB: Float) = Unit

}