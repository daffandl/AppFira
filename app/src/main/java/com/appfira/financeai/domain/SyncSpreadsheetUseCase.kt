package com.appfira.financeai.domain

import android.content.Context
import com.appfira.financeai.data.FinanceRepository
import com.appfira.financeai.logic.GoogleSheetsSync
import com.appfira.financeai.model.Transaction
import kotlinx.coroutines.flow.first

class SyncSpreadsheetUseCase(
    private val context: Context,
    private val repository: FinanceRepository
) {
    suspend fun syncTransactions(email: String, spreadsheetId: String): Int {
        val allTransactions = repository.allTransactions.first()
        val toSync = allTransactions.filter { !it.isSynced }
        
        if (toSync.isNotEmpty()) {
            val success = GoogleSheetsSync.syncTransactions(
                context, toSync, spreadsheetId, email
            )
            if (success) {
                repository.markAsSynced(toSync.map { it.id })
                return toSync.size
            }
        }
        return 0
    }

    suspend fun rewriteAllTransactions(email: String, spreadsheetId: String): Int {
        val allTransactions = repository.allTransactions.first()
        if (allTransactions.isNotEmpty()) {
            val success = GoogleSheetsSync.rewriteTransactions(
                context, allTransactions, spreadsheetId, email
            )
            if (success) {
                repository.markAsSynced(allTransactions.map { it.id })
                return allTransactions.size
            }
        }
        return 0
    }

    suspend fun listRemoteSpreadsheets(email: String) = 
        GoogleSheetsSync.listSpreadsheetsInFolder(context, email)

    suspend fun clearSheetData(email: String, spreadsheetId: String): Boolean {
        return GoogleSheetsSync.clearTransactionRows(context, spreadsheetId, email)
    }
}
