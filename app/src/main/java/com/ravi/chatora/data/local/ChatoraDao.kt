package com.ravi.chatora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
abstract class ChatoraDao {

    @Query("SELECT * FROM chatora")
    abstract fun getAllChatora(): Flow<List<ChatoraEntity>>

    @Insert
    abstract fun addChatora(chatora: ChatoraEntity)

    @Query("DELETE FROM chatora WHERE id = :id")
    abstract fun deleteChatora(id: Int)

}