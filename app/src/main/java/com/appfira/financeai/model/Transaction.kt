package com.appfira.financeai.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val description: String,
    val category: String,
    val type: TransactionType,
    val date: Long = System.currentTimeMillis(),
    @androidx.room.ColumnInfo(defaultValue = "NULL")
    val lastUpdated: Long? = null,
    val isSynced: Boolean = false
)
