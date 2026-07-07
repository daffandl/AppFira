package com.appfira.financeai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.appfira.financeai.R
import com.appfira.financeai.model.*
import com.appfira.financeai.ui.components.LogoutConfirmDialog
import com.appfira.financeai.ui.screens.settings.*
import androidx.compose.ui.res.stringResource

enum class SettingsSubScreen {
    LANGUAGE, THEME, ICON, HELP, HELP_GUIDE, HELP_SUPPORT, HELP_PRIVACY
}

@Composable
fun SettingsScreen(
    userEmail: String?,
    userName: String?,
    userPhotoUrl: String?,
    settings: AppSettings,
    paddingValues: PaddingValues,
    onLogout: () -> Unit,
    onToggleSync: (Boolean) -> Unit,
    onSetLanguage: (Language) -> Unit,
    onSetTheme: (AppTheme) -> Unit,
    onSetAppIcon: (AppIcon, Boolean) -> Unit,
    onTogglePersonal: (Boolean) -> Unit,
    onToggleShared: (Boolean) -> Unit,
    onShowBottomBar: (Boolean) -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var currentSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }

    // Handle back button for subscreens
    LaunchedEffect(currentSubScreen) {
        onShowBottomBar(currentSubScreen == null)
    }

    // Add BackHandler here to handle going back from sub-screens
    androidx.activity.compose.BackHandler(enabled = currentSubScreen != null) {
        currentSubScreen = null
    }

    if (currentSubScreen != null) {
        when (currentSubScreen) {
            SettingsSubScreen.LANGUAGE -> LanguageSubScreen(
                currentLanguage = settings.language,
                onBack = { currentSubScreen = null },
                onSetLanguage = onSetLanguage
            )
            SettingsSubScreen.THEME -> ThemeSubScreen(
                currentTheme = settings.theme,
                onBack = { currentSubScreen = null },
                onSetTheme = onSetTheme
            )
            SettingsSubScreen.ICON -> IconSubScreen(
                currentIcon = settings.appIcon,
                onBack = { currentSubScreen = null },
                onSetIcon = onSetAppIcon
            )
            SettingsSubScreen.HELP -> HelpSubScreen(
                onBack = { currentSubScreen = null },
                onNavigateTo = { currentSubScreen = it }
            )
            SettingsSubScreen.HELP_GUIDE -> HelpGuideScreen { currentSubScreen = SettingsSubScreen.HELP }
            SettingsSubScreen.HELP_SUPPORT -> HelpSupportScreen { currentSubScreen = SettingsSubScreen.HELP }
            SettingsSubScreen.HELP_PRIVACY -> HelpPrivacyScreen { currentSubScreen = SettingsSubScreen.HELP }
            else -> {}
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.nav_settings),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 100.dp)
        ) {
            item {
                ProfileSection(userName, userEmail, userPhotoUrl)
            }

            item {
                SyncSection(
                    isSyncEnabled = settings.isSyncEnabled,
                    isPersonalEnabled = settings.isPersonalSyncEnabled,
                    isSharedEnabled = settings.isSharedSyncEnabled,
                    onToggleSync = onToggleSync,
                    onTogglePersonal = onTogglePersonal,
                    onToggleShared = onToggleShared
                )
            }

            item {
                PreferenceSection(
                    language = settings.language,
                    theme = settings.theme,
                    appIcon = settings.appIcon,
                    onNavigate = { currentSubScreen = it }
                )
            }

            item {
                AboutSection(onNavigate = { currentSubScreen = it })
            }

            item {
                LogoutButton { showLogoutDialog = true }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }
}
