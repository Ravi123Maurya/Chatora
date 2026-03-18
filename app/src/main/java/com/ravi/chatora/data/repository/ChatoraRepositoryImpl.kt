package com.ravi.chatora.data.repository

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.content
import com.ravi.chatora.data.local.ChatoraDao
import com.ravi.chatora.data.mappers.toChatora
import com.ravi.chatora.data.mappers.toChatoraEntity
import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.domain.models.ChatoraHistory
import com.ravi.chatora.domain.repository.ChatoraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatoraRepositoryImpl @Inject constructor(
    private val chatoraDao: ChatoraDao,
    private val generativeModel: GenerativeModel
) : ChatoraRepository {


    override suspend fun getQueryResponse(
        query: String,
        chatoraHistory: List<Chatora>
    ): String {
        try {
            if (query.isEmpty()) return "Query is empty"

            val chatoras = generativeModel.startChat(history = chatoraHistory.map {
                content(if (it.isUser) "user" else "model") {
                    text(it.message)
                }
            }.toList())
            val chatoraResponse = chatoras.sendMessage(query)

            return chatoraResponse.text ?: "Something went wrong - repoimpl"

        } catch (e: FirebaseAIException) {
            return e.message ?: "Unknown error"
        }
    }


    override fun getAllChatoras(): Flow<List<Chatora>> {
        return chatoraDao.getAllChatora().map { chatoras ->
            chatoras.map {
                it.toChatora()
            }
        }
    }

    override fun addChatora(chatora: Chatora) {
        chatoraDao.addChatora(chatora.toChatoraEntity())
    }

    override fun deleteChatora(id: Int) {
        chatoraDao.deleteChatora(id)
    }

    override fun getChatoraHistory(): List<ChatoraHistory> {
        TODO("Not yet implemented")
    }
}