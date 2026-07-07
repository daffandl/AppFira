package com.appfira.financeai.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appfira.financeai.R
import com.appfira.financeai.logic.AppConstants
import com.appfira.financeai.logic.LocalizationUtils
import com.appfira.financeai.model.AppSettings
import com.appfira.financeai.model.Language
import com.appfira.financeai.model.Transaction
import com.appfira.financeai.model.TransactionType
import com.appfira.financeai.ui.components.DeleteTransactionDialog
import com.appfira.financeai.ui.components.getCategoryInfo
import com.appfira.financeai.ui.theme.Green
import com.appfira.financeai.ui.theme.Red
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transaction: Transaction,
    settings: AppSettings,
    onBack: () -> Unit,
    onUpdate: (Transaction) -> Unit,
    onDelete: (() -> Unit)? = null
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

    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) Green else Red
    val heroGradient = if (isIncome) {
        Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF10B981).copy(alpha = 0.85f)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFDC2626), Color(0xFFEF4444).copy(alpha = 0.85f)))
    }

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editDescription by remember { mutableStateOf(transaction.description) }
    var editAmount by remember { mutableStateOf(transaction.amount.toString()) }
    var editCategory by remember { mutableStateOf(transaction.category) }

    val categories = listOf(
        AppConstants.CAT_FOOD, AppConstants.CAT_BILL, AppConstants.CAT_TRANSPORT,
        AppConstants.CAT_SALARY, AppConstants.CAT_BONUS, AppConstants.CAT_SHOPPING,
        AppConstants.CAT_HEALTH, AppConstants.CAT_EDUCATION, AppConstants.CAT_INVESTMENT,
        AppConstants.CAT_ENTERTAINMENT, AppConstants.CAT_HOME, AppConstants.CAT_TRAVEL,
        AppConstants.CAT_CLOTHING, AppConstants.CAT_OTHER
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) stringResource(R.string.edit_transaction)
                               else stringResource(R.string.transaction_detail),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = !isEditing,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            // Sync status chip
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = if (transaction.isSynced)
                                    Color(0xFF10B981).copy(alpha = 0.12f)
                                else
                                    Color(0xFFF59E0B).copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (transaction.isSynced) Icons.Default.CloudDone
                                                      else Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = if (transaction.isSynced) Color(0xFF10B981)
                                               else Color(0xFFF59E0B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (transaction.isSynced)
                                                   stringResource(R.string.connected)
                                               else
                                                   stringResource(R.string.disconnected),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (transaction.isSynced) Color(0xFF10B981)
                                                else Color(0xFFF59E0B)
                                    )
                                }
                            }
                            // Edit button
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = stringResource(R.string.btn_edit),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            // Delete button
                            if (onDelete != null) {
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.btn_delete),
                                        tint = Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Hero Header ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroGradient)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Category icon circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryInfo.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Type badge
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = if (isIncome) stringResource(R.string.income)
                                   else stringResource(R.string.expense),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Amount
                    val formattedAmount = stringResource(
                        R.string.currency_format,
                        stringResource(R.string.currency_symbol),
                        LocalizationUtils.formatCurrency(transaction.amount, settings.language)
                    )
                    val prefix = if (isIncome) "+" else "-"
                    Text(
                        text = "$prefix $formattedAmount",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = displayDescription,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Date + Time pill row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailPill(
                            icon = Icons.Default.CalendarToday,
                            text = dateFormat.format(Date(transaction.date))
                        )
                        DetailPill(
                            icon = Icons.Default.Schedule,
                            text = timeFormat.format(Date(transaction.date))
                        )
                    }
                }
            }

            // ─── Content Section ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedContent(
                    targetState = isEditing,
                    transitionSpec = {
                        if (targetState) {
                            fadeIn(tween(250)) + slideInVertically { it / 4 } togetherWith
                                fadeOut(tween(150))
                        } else {
                            fadeIn(tween(250)) togetherWith
                                fadeOut(tween(150)) + slideOutVertically { it / 4 }
                        }
                    },
                    label = "detail_edit_anim"
                ) { editing ->
                    if (editing) {
                        // ── Edit Mode ───────────────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Card: Description & Amount
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.desc_label),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.8.sp
                                    )
                                    OutlinedTextField(
                                        value = editDescription,
                                        onValueChange = { editDescription = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = stringResource(R.string.amount_label),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.8.sp
                                    )
                                    OutlinedTextField(
                                        value = editAmount,
                                        onValueChange = { editAmount = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        leadingIcon = {
                                            Text(
                                                text = stringResource(R.string.currency_symbol),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }

                            // Card: Category picker
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.category_label),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        categories.chunked(3).forEach { rowCats ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                rowCats.forEach { cat ->
                                                    val catInfo = getCategoryInfo(cat)
                                                    val isSelected = editCategory == cat
                                                    val bgColor = if (isSelected)
                                                        catInfo.color.copy(alpha = 0.15f)
                                                    else
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                    val contentColor = if (isSelected)
                                                        catInfo.color
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant

                                                    Column(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(bgColor)
                                                            .then(
                                                                if (isSelected) Modifier.border(
                                                                    1.dp, catInfo.color.copy(alpha = 0.6f),
                                                                    RoundedCornerShape(12.dp)
                                                                ) else Modifier
                                                            )
                                                            .clickable { editCategory = cat }
                                                            .padding(vertical = 10.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Icon(
                                                            imageVector = catInfo.icon,
                                                            contentDescription = null,
                                                            tint = contentColor,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = LocalizationUtils.getLocalizedCategory(context, cat),
                                                            fontSize = 9.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = contentColor,
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                if (rowCats.size < 3) {
                                                    repeat(3 - rowCats.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Save / Cancel
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        isEditing = false
                                        editDescription = transaction.description
                                        editAmount = transaction.amount.toString()
                                        editCategory = transaction.category
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.btn_cancel), fontWeight = FontWeight.Bold)
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
                                                isSynced = false
                                            )
                                        )
                                        isEditing = false
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // ── View Mode ───────────────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Info card
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    DetailInfoRow(
                                        icon = Icons.Default.Description,
                                        iconColor = Color(0xFF3B82F6),
                                        label = stringResource(R.string.desc_label),
                                        value = displayDescription
                                    )
                                    DetailInfoRow(
                                        icon = categoryInfo.icon,
                                        iconColor = categoryInfo.color,
                                        label = stringResource(R.string.category_label),
                                        value = localizedCategory,
                                        valueColor = categoryInfo.color
                                    )
                                    DetailInfoRow(
                                        icon = Icons.Default.CalendarToday,
                                        iconColor = Color(0xFF8B5CF6),
                                        label = stringResource(R.string.date_label),
                                        value = dateFormat.format(Date(transaction.date))
                                    )
                                    DetailInfoRow(
                                        icon = Icons.Default.Schedule,
                                        iconColor = Color(0xFF06B6D4),
                                        label = stringResource(R.string.time_label),
                                        value = timeFormat.format(Date(transaction.date)),
                                        isLast = transaction.lastUpdated == null
                                    )
                                    if (transaction.lastUpdated != null) {
                                        DetailInfoRow(
                                            icon = Icons.Default.Update,
                                            iconColor = Color(0xFFF59E0B),
                                            label = stringResource(R.string.last_updated_label),
                                            value = dateFormat.format(Date(transaction.lastUpdated)) +
                                                    "  " + timeFormat.format(Date(transaction.lastUpdated)),
                                            isLast = true
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    // Delete Transaction Dialog
    if (showDeleteDialog && onDelete != null) {
        DeleteTransactionDialog(
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                onDelete()
                onBack()
            }
        )
    }
}

// ─── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun DetailPill(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored icon box
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.38f)
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.62f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!isLast) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
            )
        }
    }
}
