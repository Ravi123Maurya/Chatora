package com.ravi.chatora.presentation.chatora

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.domain.repository.ChatoraRepository
import com.ravi.chatora.ui.theme.AppColors
import com.ravi.chatora.utils.formatTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// ─── Segments ─────────────────────────────────────────────────────────────────
sealed class Segment() {
    data class Markdown(val annotatedContent: AnnotatedString) : Segment()
    data class CodeBlock(val language: String, val code: String) : Segment()
}

@Immutable
data class ParsedChatora(
    val id: Int,
    val isUser: Boolean,
    val message: String,
    val parsedMessage: List<Segment>,
    val timeStamp: String,
)

@HiltViewModel
class ChatoraViewModel @Inject constructor(
    private val chatoraRepository: ChatoraRepository
) : ViewModel() {


    private val _getChatoraResponseState =
        MutableStateFlow<GetChatoraResponse>(GetChatoraResponse.Idle)
    val getChatoraResponseState = _getChatoraResponseState.asStateFlow()

    private val _chatoras = MutableStateFlow<List<Chatora>>(emptyList())
    val chatoras = _chatoras.asStateFlow()

    private val _parsedChatoras = MutableStateFlow<List<ParsedChatora>>(emptyList())
    val parsedChatoras = _parsedChatoras.asStateFlow()

    init {
        getAllChatoras()
        viewModelScope.launch {
            combine(chatoras, getChatoraResponseState) { dbMessages, responseState ->
                val currentList = dbMessages.toMutableList()

                if (responseState is GetChatoraResponse.Loading) {
                    currentList.add(
                        Chatora(
                            message = "Generating...",
                            isUser = false,
                            timeStamp = ""
                        )
                    )
                }

                currentList // Emit the final combined list
            }.collect { combinedList ->
                withContext(Dispatchers.Default) {
                    _parsedChatoras.value = combinedList.map { chatora ->
                        val textColor =
                            if (chatora.isUser) AppColors.OnPrimary else AppColors.OnSurface
                        ParsedChatora(
                            id = chatora.id,
                            isUser = chatora.isUser,
                            message = chatora.message,
                            parsedMessage = parseSegments(chatora.message, textColor),
                            timeStamp = chatora.timeStamp
                        )
                    }
                }
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


    private fun getAllChatoras() {
        viewModelScope.launch(Dispatchers.IO) {
            chatoraRepository.getAllChatoras().collect {
                _chatoras.value = it
            }
        }
    }

    private fun addChatora(chatora: Chatora) {
        viewModelScope.launch(Dispatchers.IO) {
            chatoraRepository.addChatora(chatora)
        }
    }


    fun deleterChatora(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            chatoraRepository.deleteChatora(id)
        }
    }
}

sealed class GetChatoraResponse {
    object Idle : GetChatoraResponse()
    object Loading : GetChatoraResponse()
}



// Chatora Parsing Logic ----------------------------------
fun parseSegments(input: String, textColor: Color): List<Segment> {
    val result = mutableListOf<Segment>()
    val fenceRegex = Regex("""```(\w*)\n([\s\S]*?)```""")
    var lastEnd = 0
    fenceRegex.findAll(input).forEach { match ->
        if (match.range.first > lastEnd) {
            val aContent = buildMarkdownAnnotatedString(
                text =input.substring(
                    lastEnd,
                    match.range.first
                ),
                textColor = textColor
            )
            result += Segment.Markdown(aContent)
        }
        result += Segment.CodeBlock(
            match.groupValues[1].ifEmpty { "code" },
            match.groupValues[2].trimEnd()
        )
        lastEnd = match.range.last + 1
    }
    if (lastEnd < input.length){
        val aContent = buildMarkdownAnnotatedString(input.substring(lastEnd), textColor)
        result += Segment.Markdown(aContent)
    }
    return result
}

// ─── Markdown Builder ─────────────────────────────────────────────────────────

fun buildMarkdownAnnotatedString(text: String, textColor: Color): AnnotatedString {
    val headingStyle = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
    val h3Style = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    val boldStyle = SpanStyle(fontWeight = FontWeight.Bold, color = AppColors.Primary)
    val inlineCodeStyle = SpanStyle(
        fontFamily = FontFamily.Monospace, fontSize = 12.sp,
        background = Color(0xFFE8F0FF), color = Color(0xFF3358CC)
    )
    val bulletParagraph = ParagraphStyle(textIndent = TextIndent(restLine = 8.sp))

    return buildAnnotatedString {
        text.trimEnd().lines().forEachIndexed { index, line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("## ") -> {
                    withStyle(headingStyle) { append(trimmed.removePrefix("## ")) }
                    append("\n")
                }

                trimmed.startsWith("### ") -> {
                    withStyle(h3Style) { append(trimmed.removePrefix("### ")) }
                    append("\n")
                }

                trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    val content = trimmed.removePrefix("* ").removePrefix("- ")
                    withStyle(bulletParagraph) {
                        append("• ")
                        processInlineStyles(content, boldStyle, inlineCodeStyle)
                    }
                    append("\n")
                }

                trimmed.isEmpty() -> append("\n")
                else -> {
                    processInlineStyles(trimmed, boldStyle, inlineCodeStyle)
                    if (index < text.trimEnd().lines().lastIndex) append("\n")
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.processInlineStyles(
    text: String, boldStyle: SpanStyle, inlineCodeStyle: SpanStyle
) {
    val pattern = Regex("""\*\*(.*?)\*\*|`([^`]+)`""")
    var lastIndex = 0
    pattern.findAll(text).forEach { match ->
        if (match.range.first > lastIndex) append(text.substring(lastIndex, match.range.first))
        when {
            match.groupValues[1].isNotEmpty() -> withStyle(boldStyle) { append(match.groupValues[1]) }
            match.groupValues[2].isNotEmpty() -> withStyle(inlineCodeStyle) { append(" ${match.groupValues[2]} ") }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) append(text.substring(lastIndex))
}