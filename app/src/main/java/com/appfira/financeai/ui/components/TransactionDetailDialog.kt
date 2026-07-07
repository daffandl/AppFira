package com.appfira.financeai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.appfira.financeai.R
import com.appfira.financeai.logic.AppConstants
import com.appfira.financeai.logic.LocalizationUtils
import com.appfira.financeai.model.AppSettings
import com.appfira.financeai.model.Language
import com.appfira.financeai.model.Transaction
import com.appfira.financeai.model.TransactionType
import com.appfira.financeai.ui.theme.Green
import com.appfira.financeai.ui.theme.Red
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailDialog(
    transaction: Transaction,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onUpdate: (Transaction) -> Unit = {}
) {
    val context = LocalContext.current
    val locale = when (settings.language) {
        Language.INDONESIAN -> Locale("id", "ID")
        Language.ENGLISH -> Locale.US
        Language.SYSTEM -> Locale.getDefault()
    }
    val dateFormat = remember(locale) { SimpleDateFormat("dd MMMM yyyy", locale) }
    val timeFormat = remember(locale) { SimpleDateFormat("HH:mm:ss", locale) }
    val categoryInfo = getCategoryInfo(transaction.category)
    
    val localizedCategory = LocalizationUtils.getLocalizedCategory(context, transaction.category)
    val displayDescription = LocalizationUtils.getLocalizedDescription(context, transaction.description)
    val amountColor = if (transaction.type == TransactionType.INCOME) Green else Red

    var isEditing by remember { mutableStateOf(false) }
    var editDescription by remember { mutableStateOf(transaction.description) }
    var editAmount by remember { mutableStateOf(transaction.amount.toString()) }
    var editCategory by remember { mutableStateOf(transaction.category) }
    var expandedCategory by remember { mutableStateOf(false) }

    val categories = listOf(
        AppConstants.CAT_FOOD, AppConstants.CAT_BILL, AppConstants.CAT_TRANSPORT, 
        AppConstants.CAT_SALARY, AppConstants.CAT_BONUS, AppConstants.CAT_SHOPPING, 
        AppConstants.CAT_HEALTH, AppConstants.CAT_EDUCATION, AppConstants.CAT_INVESTMENT, 
        AppConstants.CAT_ENTERTAINMENT, AppConstants.CAT_HOME, AppConstants.CAT_TRAVEL, 
        AppConstants.CAT_CLOTHING, AppConstants.CAT_OTHER
    )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(categoryInfo.color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryInfo.icon,
                                contentDescription = null,
                                tint = categoryInfo.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.transaction_detail),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (transaction.type == TransactionType.INCOME) stringResource(R.string.income) else stringResource(R.string.expense),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = amountColor
                            )
                        }
                    }
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text(stringResource(R.string.desc_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text(stringResource(R.string.amount_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory }
                    ) {
                        OutlinedTextField(
                            value = LocalizationUtils.getLocalizedCategory(context, editCategory),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.category_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(LocalizationUtils.getLocalizedCategory(context, cat)) },
                                    onClick = {
                                        editCategory = cat
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    DetailRow(stringResource(R.string.desc_label), displayDescription, Icons.Default.Description)
                    DetailRow(stringResource(R.string.category_label), localizedCategory, categoryInfo.icon)
                    val formattedAmount = stringResource(
                        R.string.currency_format,
                        stringResource(R.string.currency_symbol),
                        LocalizationUtils.formatCurrency(transaction.amount, settings.language)
                    )
                    DetailRow(stringResource(R.string.amount_label), formattedAmount, Icons.Default.Payments, valueColor = amountColor)
                    DetailRow(stringResource(R.string.date_label), dateFormat.format(Date(transaction.date)), Icons.Default.CalendarToday)
                    DetailRow(stringResource(R.string.time_label), timeFormat.format(Date(transaction.date)), Icons.Default.Schedule)
                    
                    if (transaction.lastUpdated != null) {
                        DetailRow(
                            if (settings.language == Language.ENGLISH) "Last Updated" else "Terakhir Diubah", 
                            dateFormat.format(Date(transaction.lastUpdated)) + " " + timeFormat.format(Date(transaction.lastUpdated)), 
                            Icons.Default.Update
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (isEditing) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (settings.language == Language.ENGLISH) "Cancel" else "Batal", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { 
                                val amountValue = editAmount.toLongOrNull() ?: transaction.amount
                                onUpdate(
                                    transaction.copy(
                                        description = editDescription,
                                        amount = amountValue,
                                        category = editCategory,
                                        lastUpdated = System.currentTimeMillis(),
                                        isSynced = false // mark as not synced so it syncs to sheets
                                    )
                                )
                                isEditing = false
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (settings.language == Language.ENGLISH) "Save" else "Simpan", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.close_btn),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.4f)
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.6f)
            )
        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
        )
    }
}
