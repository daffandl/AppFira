package com.appfira.financeai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appfira.financeai.R
import com.appfira.financeai.model.AppSettings
import com.appfira.financeai.model.Language
import com.appfira.financeai.model.SpreadsheetInfo
import com.appfira.financeai.ui.components.*
import com.appfira.financeai.ui.theme.cardBorder
import com.appfira.financeai.ui.theme.cardBorderAccent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SpreadsheetScreen(
    settings: AppSettings,
    paddingValues: PaddingValues,
    onCreateNew: (String) -> Unit,
    onSelectActive: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDeleteFromDrive: (String) -> Unit,
    onToggleSync: (Boolean) -> Unit,
    onOpenLink: (String) -> Unit,
    onShareLink: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf<String?>(null) }
    var deleteConfirmSheet by remember { mutableStateOf<SpreadsheetInfo?>(null) }
    val isIndo = settings.language == Language.INDONESIAN
    
    val activeSheet = settings.spreadsheets.find { it.id == settings.activeSpreadsheetId }
    val locale = if (settings.language == Language.INDONESIAN) Locale("id", "ID") else Locale.US
    val dateFormatter = remember(locale) { SimpleDateFormat("dd MMM yyyy", locale) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Active Spreadsheet Card
        if (activeSheet != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = cardBorderAccent(MaterialTheme.colorScheme.primary),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.active_spreadsheet),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { onOpenLink(activeSheet.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.OpenInNew, contentDescription = stringResource(R.string.open_label), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRowSpreadsheet(stringResource(R.string.spreadsheet_label_name), activeSheet.title)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.copy_id), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(activeSheet.id, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRowSpreadsheet(stringResource(R.string.spreadsheet_label_created), dateFormatter.format(Date(activeSheet.createdTime)))
                    }
            }
        } else {
            // Placeholder card if no active spreadsheet
            Card(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = cardBorder(),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.no_active_spreadsheet), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create Button
        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.create_new_spreadsheet), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (settings.spreadsheets.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.no_spreadsheets), 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 80.dp)
            ) {
                items(settings.spreadsheets) { sheet ->
                    SpreadsheetItem(
                        sheet = sheet,
                        isActive = sheet.id == settings.activeSpreadsheetId,
                        onClick = { onSelectActive(sheet.id) },
                        onShare = { onShareLink(sheet.id) },
                        onQrCode = { showQrDialog = sheet.id },
                        onDelete = { deleteConfirmSheet = sheet }
                    )
                }
            }
        }
    }

    if (showQrDialog != null) {
        QrCodeDialog(spreadsheetId = showQrDialog!!, isIndo = isIndo, onDismiss = { showQrDialog = null })
    }

    if (deleteConfirmSheet != null) {
        DeleteConfirmDialog(
            title = deleteConfirmSheet!!.title,
            onDismiss = { deleteConfirmSheet = null },
            onDelete = { onDeleteFromDrive(deleteConfirmSheet!!.id) }
        )
    }

    if (showCreateDialog) {
        CreateSpreadsheetDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { onCreateNew(it) }
        )
    }
}

@Composable
fun DetailRowSpreadsheet(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
