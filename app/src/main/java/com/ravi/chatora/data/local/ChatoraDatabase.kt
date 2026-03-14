package com.ravi.chatora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [ChatoraEntity::class],
    version = 1,
)
abstract class ChatoraDatabase : RoomDatabase() {

    abstract fun chatoraDao(): ChatoraDao

}