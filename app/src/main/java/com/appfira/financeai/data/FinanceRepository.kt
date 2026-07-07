package com.appfira.financeai.data

import com.appfira.financeai.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val settingsDao: SettingsDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val totalIncome: Flow<Long?> = transactionDao.getTotalIncome()
    val totalExpense: Flow<Long?> = transactionDao.getTotalExpense()

    val appSettings: Flow<AppSettings> = settingsDao.getSettings().map { entity ->
        if (entity == null) AppSettings()
        else {
            val language = try {
                Language.valueOf(entity.language)
            } catch (e: Exception) {
                Language.INDONESIAN
            }
            val theme = try {
                AppTheme.valueOf(entity.theme)
            } catch (e: Exception) {
                AppTheme.SYSTEM
            }
            val appIcon = try {
                AppIcon.valueOf(entity.appIcon)
            } catch (e: Exception) {
                AppIcon.DEFAULT
            }
            
            AppSettings(
                spreadsheets = entity.spreadsheets,
                activeSpreadsheetId = entity.activeSpreadsheetId,
                isSyncEnabled = entity.isSyncEnabled,
                language = language,
                theme = theme,
                appIcon = appIcon,
                personalSpreadsheetId = entity.personalSpreadsheetId,
                sharedSpreadsheetId = entity.sharedSpreadsheetId,
                isPersonalSyncEnabled = entity.isPersonalSyncEnabled,
                isSharedSyncEnabled = entity.isSharedSyncEnabled
            )
        }
    }

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun markAsSynced(ids: List<Long>) {
        transactionDao.markAsSynced(ids)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteAll() {
        transactionDao.deleteAllTransactions()
    }

    suspend fun saveSettings(settings: AppSettings) {
        val entity = SettingsEntity(
            spreadsheets = settings.spreadsheets,
            activeSpreadsheetId = settings.activeSpreadsheetId,
            isSyncEnabled = settings.isSyncEnabled,
            language = settings.language.name,
            theme = settings.theme.name,
            appIcon = settings.appIcon.name,
            personalSpreadsheetId = settings.personalSpreadsheetId,
            sharedSpreadsheetId = settings.sharedSpreadsheetId,
            isPersonalSyncEnabled = settings.isPersonalSyncEnabled,
            isSharedSyncEnabled = settings.isSharedSyncEnabled
        )
        settingsDao.saveSettings(entity)
    }
}
