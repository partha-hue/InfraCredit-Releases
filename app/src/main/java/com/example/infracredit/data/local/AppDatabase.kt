package com.example.infracredit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.infracredit.data.local.dao.CustomerDao
import com.example.infracredit.data.local.dao.TransactionDao
import com.example.infracredit.data.local.entity.CustomerEntity
import com.example.infracredit.data.local.entity.TransactionEntity

@Database(
    entities = [CustomerEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "infracredit.db"
    }
}
