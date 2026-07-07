package com.appfira.financeai.model

enum class SpreadsheetType {
    PERSONAL, SHARED
}

enum class Language {
    SYSTEM, INDONESIAN, ENGLISH
}

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

enum class AppIcon {
    DEFAULT, DARK, THEMED
}

data class SpreadsheetInfo(
    val id: String,
    val title: String,
    val type: SpreadsheetType = SpreadsheetType.PERSONAL,
    val createdTime: Long = System.currentTimeMillis()
)

data class AppSettings(
    val spreadsheets: List<SpreadsheetInfo> = emptyList(),
    val activeSpreadsheetId: String? = null,
    val isSyncEnabled: Boolean = false,
    val language: Language = Language.SYSTEM,
    val theme: AppTheme = AppTheme.SYSTEM,
    val appIcon: AppIcon = AppIcon.DEFAULT,
    
    // Legacy support (to be migrated or removed)
    val personalSpreadsheetId: String? = null,
    val sharedSpreadsheetId: String? = null,
    val isPersonalSyncEnabled: Boolean = false,
    val isSharedSyncEnabled: Boolean = false
)
