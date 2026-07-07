package com.appfira.financeai.domain

import android.content.Context
import com.appfira.financeai.data.FinanceRepository
import com.appfira.financeai.logic.GoogleSheetsSync
import com.appfira.financeai.model.SpreadsheetInfo
import kotlinx.coroutines.flow.first

class ManageSpreadsheetUseCase(
    private val context: Context,
    private val repository: FinanceRepository
) {
    suspend fun createSpreadsheet(email: String, title: String): String? {
        return GoogleSheetsSync.createSpreadsheet(context, email, title = title)
    }

    suspend fun deleteSpreadsheet(email: String, id: String): Boolean {
        return GoogleSheetsSync.deleteSpreadsheet(context, email, id)
    }

    suspend fun updateLocalSpreadsheets(id: String, title: String) {
        val currentSettings = repository.appSettings.first()
        val newInfo = SpreadsheetInfo(id, title)
        val newList = currentSettings.spreadsheets + newInfo
        val newSettings = currentSettings.copy(
            spreadsheets = newList,
            activeSpreadsheetId = id
        )
        repository.saveSettings(newSettings)
    }
    
    suspend fun removeLocalSpreadsheet(id: String) {
        val currentSettings = repository.appSettings.first()
        val newList = currentSettings.spreadsheets.filter { it.id != id }
        var activeId = currentSettings.activeSpreadsheetId
        if (activeId == id) {
            activeId = newList.firstOrNull()?.id
        }
        
        val newSettings = currentSettings.copy(
            spreadsheets = newList,
            activeSpreadsheetId = activeId
        )
        repository.saveSettings(newSettings)
    }
}
