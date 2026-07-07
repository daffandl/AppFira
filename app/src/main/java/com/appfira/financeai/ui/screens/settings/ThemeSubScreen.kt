package com.appfira.financeai.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appfira.financeai.model.AppTheme
import com.appfira.financeai.R

@Composable
fun ThemeSubScreen(
    currentTheme: AppTheme,
    onBack: () -> Unit,
    onSetTheme: (AppTheme) -> Unit
) {
    SubScreenLayout(
        title = stringResource(R.string.theme_select_title),
        onBack = onBack
    ) {
        SettingsCard {
            Column(modifier = Modifier.padding(4.dp)) {
                SelectionItem(
                    label = stringResource(R.string.theme_system),
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = currentTheme == AppTheme.SYSTEM,
                    onClick = { onSetTheme(AppTheme.SYSTEM) }
                )
                SelectionItem(
                    label = stringResource(R.string.theme_light),
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == AppTheme.LIGHT,
                    onClick = { onSetTheme(AppTheme.LIGHT) }
                )
                SelectionItem(
                    label = stringResource(R.string.theme_dark),
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == AppTheme.DARK,
                    showDivider = false,
                    onClick = { onSetTheme(AppTheme.DARK) }
                )
            }
        }
    }
}
