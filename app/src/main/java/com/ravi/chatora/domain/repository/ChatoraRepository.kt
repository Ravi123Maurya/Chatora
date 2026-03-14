package com.ravi.chatora.domain.repository

import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatoraRepository {
   suspend fun getQueryResponse(query: String, chatoraHistory: List<Chatora>) : String

   fun getAllChatoras() : Flow<List<Chatora>>

   fun addChatora(chatora: Chatora)

   fun deleteChatora(id: Int)

}