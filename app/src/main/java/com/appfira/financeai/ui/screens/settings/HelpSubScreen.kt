package com.appfira.financeai.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appfira.financeai.R
import com.appfira.financeai.ui.screens.SettingsSubScreen
import com.appfira.financeai.ui.theme.cardBorder

@Composable
fun HelpSubScreen(
    onBack: () -> Unit,
    onNavigateTo: (SettingsSubScreen) -> Unit
) {
    SubScreenLayout(
        title = stringResource(R.string.help_title),
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HelpCard(
                    title = stringResource(R.string.help_sync_guide_title),
                    description = stringResource(R.string.help_guide_short_desc),
                    icon = Icons.Default.MenuBook,
                    onClick = { onNavigateTo(SettingsSubScreen.HELP_GUIDE) }
                )
            }

            item {
                HelpCard(
                    title = stringResource(R.string.help_more_title),
                    description = stringResource(R.string.support_email_label),
                    icon = Icons.Default.SupportAgent,
                    onClick = { onNavigateTo(SettingsSubScreen.HELP_SUPPORT) }
                )
            }

            item {
                HelpCard(
                    title = stringResource(R.string.help_privacy_title),
                    description = stringResource(R.string.help_privacy_short_desc),
                    icon = Icons.Default.Shield,
                    onClick = { onNavigateTo(SettingsSubScreen.HELP_PRIVACY) }
                )
            }
        }
    }
}

@Composable
fun HelpCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = cardBorder()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun HelpGuideScreen(onBack: () -> Unit) {
    SubScreenLayout(title = stringResource(R.string.help_sync_guide_title), onBack = onBack) {
        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.help_features_title), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Text(stringResource(R.string.help_feature_ai))
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.help_feature_dashboard))
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.help_feature_sync))
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.help_feature_export))

                Spacer(modifier = Modifier.height(24.dp))

                Text(stringResource(R.string.help_sync_guide_title), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Text(stringResource(R.string.help_sync_step_1))
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.help_sync_step_2))
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.help_sync_step_3))
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.help_sync_step_4))
            }
        }
    }
}

@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    SubScreenLayout(title = stringResource(R.string.help_more_title), onBack = onBack) {
        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.help_more_title), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.support_email_label))
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.help_support_response_time), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun HelpPrivacyScreen(onBack: () -> Unit) {
    SubScreenLayout(title = stringResource(R.string.help_privacy_title), onBack = onBack) {
        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.help_privacy_title), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.help_privacy_desc))
            }
        }
    }
}
