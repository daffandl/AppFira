package com.appfira.financeai

import android.app.Application
import androidx.lifecycle.*
import com.appfira.financeai.model.*
import com.appfira.financeai.ui.components.ChatMessage
import com.appfira.financeai.R
import com.appfira.financeai.domain.ManageSpreadsheetUseCase
import com.appfira.financeai.domain.ProcessChatUseCase
import com.appfira.financeai.domain.SyncSpreadsheetUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanceViewModel(
    application: Application,
    private val repository: com.appfira.financeai.data.FinanceRepository,
    private val processChatUseCase: ProcessChatUseCase,
    private val syncSpreadsheetUseCase: SyncSpreadsheetUseCase,
    private val manageSpreadsheetUseCase: ManageSpreadsheetUseCase
) : AndroidViewModel(application) {

    val transactions = repository.allTransactions
    val totalIncome = repository.totalIncome
    val totalExpense = repository.totalExpense

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    val settings: StateFlow<AppSettings?> = repository.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl: StateFlow<String?> = _userPhotoUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setGoogleAccount(email: String?, name: String?, photoUrl: String?) {
        _userEmail.value = email
        _userName.value = name
        _userPhotoUrl.value = photoUrl
    }

    fun syncFromDrive(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val remoteFiles = syncSpreadsheetUseCase.listRemoteSpreadsheets(email)
                val currentSettings = settings.value ?: repository.appSettings.first()
                
                if (remoteFiles.isNotEmpty()) {
                    val currentIds = currentSettings.spreadsheets.map { it.id }.toSet()
                    val newSpreadsheets = remoteFiles
                        .filter { it.id !in currentIds }
                        .map { SpreadsheetInfo(it.id, it.name, createdTime = it.createdTime?.value ?: System.currentTimeMillis()) }
                    
                    if (newSpreadsheets.isNotEmpty()) {
                        val updatedList = currentSettings.spreadsheets + newSpreadsheets
                        val newSettings = currentSettings.copy(
                            spreadsheets = updatedList,
                            activeSpreadsheetId = currentSettings.activeSpreadsheetId ?: updatedList.firstOrNull()?.id
                        )
                        saveSettings(newSettings)
                    }
                }
            } catch (e: Exception) {
                // Silently fail or log
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _userEmail.value = null
            _userName.value = null
            _userPhotoUrl.value = null
            _messages.value = emptyList()
            repository.saveSettings(AppSettings()) // Reset to defaults
        }
    }

    fun processInput(text: String, forcedType: TransactionType? = null) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _messages.update { it + ChatMessage(text, true) }
            
            val currentSettings = settings.value ?: AppSettings()
            val localizedContext = com.appfira.financeai.logic.LocalizationUtils.setLocale(getApplication(), currentSettings.language)
            
            try {
                val transaction = processChatUseCase(text, forcedType)
                
                if (transaction != null) {
                    val account = _userEmail.value
                    val activeId = currentSettings.activeSpreadsheetId
                    
                    if (currentSettings.isSyncEnabled && activeId != null && account != null) {
                        syncSpreadsheetUseCase.syncTransactions(account, activeId)
                    }
                    
                    val typeText = if (transaction.type == TransactionType.INCOME) 
                        localizedContext.getString(R.string.type_income_label) 
                    else 
                        localizedContext.getString(R.string.type_expense_label)
                    
                    val categoryText = com.appfira.financeai.logic.LocalizationUtils.getLocalizedCategory(localizedContext, transaction.category)
                    val displayDescription = com.appfira.financeai.logic.LocalizationUtils.getLocalizedDescription(localizedContext, transaction.description)
                    
                    val formattedAmount = localizedContext.getString(
                        R.string.currency_format,
                        localizedContext.getString(R.string.currency_symbol),
                        com.appfira.financeai.logic.LocalizationUtils.formatCurrency(transaction.amount, currentSettings.language)
                    )

                    val successMessage = localizedContext.getString(
                        R.string.chat_parse_success,
                        typeText,
                        displayDescription,
                        formattedAmount,
                        categoryText
                    )
                    
                    _messages.update { it + ChatMessage(successMessage, false) }
                } else {
                    _messages.update { it + ChatMessage(localizedContext.getString(R.string.chat_parse_fail), false) }
                }
            } catch (e: Exception) {
                _messages.update { it + ChatMessage(localizedContext.getString(R.string.chat_parse_fail), false) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setLanguage(language: Language) {
        settings.value?.let { current ->
            saveSettings(current.copy(language = language))
        }
    }

    fun setTheme(theme: AppTheme) {
        settings.value?.let { current ->
            saveSettings(current.copy(theme = theme))
        }
    }

    fun setAppIcon(icon: AppIcon, immediate: Boolean = false) {
        settings.value?.let { current ->
            saveSettings(current.copy(appIcon = icon))
            if (immediate) {
                com.appfira.financeai.logic.Utils.changeAppIcon(getApplication(), icon, true)
            }
        }
    }

    fun setActiveSpreadsheet(id: String) {
        settings.value?.let { current ->
            saveSettings(current.copy(activeSpreadsheetId = id))
        }
    }

    fun deleteSpreadsheet(id: String) {
        viewModelScope.launch {
            manageSpreadsheetUseCase.removeLocalSpreadsheet(id)
        }
    }

    fun deleteSpreadsheetFromDrive(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val account = _userEmail.value
                val success = if (account != null) {
                    manageSpreadsheetUseCase.deleteSpreadsheet(account, id)
                } else {
                    true // Tidak ada akun, langsung hapus lokal
                }
                if (success) {
                    manageSpreadsheetUseCase.removeLocalSpreadsheet(id)
                }
            } catch (e: Exception) {
                // Log error jika perlu
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.update(transaction)
                
                val account = _userEmail.value
                val currentSettings = settings.value ?: repository.appSettings.first()
                val activeId = currentSettings.activeSpreadsheetId
                if (currentSettings.isSyncEnabled && activeId != null && account != null) {
                    syncSpreadsheetUseCase.rewriteAllTransactions(account, activeId)
                }
            } catch (e: Exception) {
                // Log error jika perlu
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(transaction: Transaction, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.delete(transaction)

                val account = _userEmail.value
                val currentSettings = settings.value ?: repository.appSettings.first()
                val activeId = currentSettings.activeSpreadsheetId
                if (currentSettings.isSyncEnabled && activeId != null && account != null) {
                    // Rewrite all remaining transactions to sheet
                    syncSpreadsheetUseCase.rewriteAllTransactions(account, activeId)
                }
            } catch (e: Exception) {
                // Log error jika perlu
            } finally {
                _isLoading.value = false
                onDone()
            }
        }
    }

    fun deleteAllTransactions(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteAll()

                val account = _userEmail.value
                val currentSettings = settings.value ?: repository.appSettings.first()
                val activeId = currentSettings.activeSpreadsheetId
                if (currentSettings.isSyncEnabled && activeId != null && account != null) {
                    // Clear data rows in sheet but keep header intact
                    syncSpreadsheetUseCase.clearSheetData(account, activeId)
                }
            } catch (e: Exception) {
                // Log error jika perlu
            } finally {
                _isLoading.value = false
                onDone()
            }
        }
    }

    fun createNewSpreadsheet(title: String) {
        viewModelScope.launch {
            val account = _userEmail.value
            val currentSettings = settings.value ?: return@launch
            val localizedContext = com.appfira.financeai.logic.LocalizationUtils.setLocale(getApplication(), currentSettings.language)
            
            if (account == null) {
                _messages.update { it + ChatMessage(
                    localizedContext.getString(R.string.chat_login_required), 
                    false
                ) }
                return@launch
            }
            
            _messages.update { it + ChatMessage(
                localizedContext.getString(R.string.chat_creating_spreadsheet, title), 
                false
            ) }
            val id = manageSpreadsheetUseCase.createSpreadsheet(account, title)
            
            if (id != null) {
                manageSpreadsheetUseCase.updateLocalSpreadsheets(id, title)
                
                val settingsSnapshot = settings.value ?: return@launch
                val canEnableSync = settingsSnapshot.isPersonalSyncEnabled || settingsSnapshot.isSharedSyncEnabled
                
                if (canEnableSync) {
                    syncSpreadsheetUseCase.syncTransactions(account, id)
                }

                _messages.update { it + ChatMessage(
                    localizedContext.getString(R.string.chat_spreadsheet_success, title), 
                    false
                ) }
            } else {
                _messages.update { it + ChatMessage(
                    localizedContext.getString(R.string.chat_spreadsheet_fail), 
                    false
                ) }
            }
        }
    }

    fun toggleSync(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = repository.appSettings.first()
            val localizedContext = com.appfira.financeai.logic.LocalizationUtils.setLocale(getApplication(), currentSettings.language)
            
            if (enabled && !currentSettings.isPersonalSyncEnabled && !currentSettings.isSharedSyncEnabled) {
                _messages.update { it + ChatMessage(
                    localizedContext.getString(R.string.chat_sync_auto_fail), 
                    false
                ) }
                return@launch
            }

            val account = _userEmail.value
            val activeId = currentSettings.activeSpreadsheetId
            
            if (enabled && account == null) {
                _messages.update { it + ChatMessage(
                    localizedContext.getString(R.string.chat_login_required), 
                    false
                ) }
                return@launch
            }

            repository.saveSettings(currentSettings.copy(isSyncEnabled = enabled))
            
            if (enabled && activeId != null && account != null) {
                val syncedCount = syncSpreadsheetUseCase.syncTransactions(account, activeId)
                
                if (syncedCount > 0) {
                    _messages.update { it + ChatMessage(
                        localizedContext.getString(R.string.chat_sync_enabled_new, syncedCount), 
                        false
                    ) }
                } else {
                    _messages.update { it + ChatMessage(
                        localizedContext.getString(R.string.chat_sync_enabled_none), 
                        false
                    ) }
                }
                syncFromDrive(account)
            }
        }
    }

    fun toggleSharedSync(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = repository.appSettings.first()
            val willHaveAnySync = enabled || currentSettings.isPersonalSyncEnabled
            val newSettings = currentSettings.copy(
                isSharedSyncEnabled = enabled,
                isSyncEnabled = if (!willHaveAnySync) false else currentSettings.isSyncEnabled
            )
            repository.saveSettings(newSettings)
            
            if (enabled) {
                _userEmail.value?.let { syncFromDrive(it) }
            }
        }
    }

    fun togglePersonalSync(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = repository.appSettings.first()
            val willHaveAnySync = enabled || currentSettings.isSharedSyncEnabled
            val newSettings = currentSettings.copy(
                isPersonalSyncEnabled = enabled,
                isSyncEnabled = if (!willHaveAnySync) false else currentSettings.isSyncEnabled
            )
            repository.saveSettings(newSettings)

            if (enabled) {
                _userEmail.value?.let { syncFromDrive(it) }
            }
        }
    }
}
