package com.appfira.financeai.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appfira.financeai.model.Language
import com.appfira.financeai.R

@Composable
fun LanguageSubScreen(
    currentLanguage: Language,
    onBack: () -> Unit,
    onSetLanguage: (Language) -> Unit
) {
    SubScreenLayout(
        title = stringResource(R.string.lang_select_title),
        onBack = onBack
    ) {
        SettingsCard {
            Column(modifier = Modifier.padding(4.dp)) {
                SelectionItem(
                    label = stringResource(R.string.theme_system),
                    icon = Icons.Default.Language,
                    isSelected = currentLanguage == Language.SYSTEM,
                    onClick = { onSetLanguage(Language.SYSTEM) }
                )
                SelectionItem(
                    label = stringResource(R.string.lang_name_indo),
                    icon = Icons.Default.Language,
                    isSelected = currentLanguage == Language.INDONESIAN,
                    onClick = { onSetLanguage(Language.INDONESIAN) }
                )
                SelectionItem(
                    label = stringResource(R.string.lang_name_en),
                    icon = Icons.Default.Translate,
                    isSelected = currentLanguage == Language.ENGLISH,
                    showDivider = false,
                    onClick = { onSetLanguage(Language.ENGLISH) }
                )
            }
        }
    }
}
