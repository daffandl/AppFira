package com.appfira.financeai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appfira.financeai.R
import com.appfira.financeai.logic.AppConstants
import com.appfira.financeai.logic.LocalizationUtils
import com.appfira.financeai.model.AppSettings
import com.appfira.financeai.model.Language
import com.appfira.financeai.model.Transaction
import com.appfira.financeai.ui.components.CategoryChip
import com.appfira.financeai.ui.components.TransactionRow
import com.appfira.financeai.ui.theme.cardBorder
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    settings: AppSettings,
    paddingValues: PaddingValues,
    onExport: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val allLabel = stringResource(R.string.category_all)
    
    var selectedCategory by remember { mutableStateOf(allLabel) }
    
    val categoryIcons = remember {
        mapOf(
            AppConstants.CAT_FOOD to Icons.Default.Restaurant,
            AppConstants.CAT_TRANSPORT to Icons.Default.DirectionsCar,
            AppConstants.CAT_ENTERTAINMENT to Icons.Default.Movie,
            AppConstants.CAT_SHOPPING to Icons.Default.ShoppingBag,
            AppConstants.CAT_HEALTH to Icons.Default.MedicalServices,
            AppConstants.CAT_EDUCATION to Icons.Default.School,
            AppConstants.CAT_SALARY to Icons.Default.Payments,
            AppConstants.CAT_INVESTMENT to Icons.Default.TrendingUp,
            AppConstants.CAT_OTHER to Icons.Default.Category
        )
    }

    val categories = transactions.groupBy { it.category }
    val categoryList = listOf(allLabel) + categories.keys.sorted()
    
    val filteredTransactions = if (selectedCategory == allLabel) {
        transactions
    } else {
        transactions.filter { it.category == selectedCategory }
    }

    val locale = when (settings.language) {
        Language.INDONESIAN -> Locale("id", "ID")
        Language.ENGLISH -> Locale.US
        Language.SYSTEM -> Locale.getDefault()
    }
    val dateFormatter = remember(locale) { SimpleDateFormat("dd MMMM yyyy", locale) }
    val groupedTransactions = remember(filteredTransactions, locale) {
        filteredTransactions.groupBy { 
            dateFormatter.format(Date(it.date))
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.history_title), fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Button(
                onClick = onExport,
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.export_label), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categoryList) { category ->
                val isSelected = selectedCategory == category
                val count = if (category == allLabel) transactions.size else categories[category]?.size ?: 0
                val icon = categoryIcons[category] ?: Icons.Default.Category
                val context = LocalContext.current
                
                CategoryChip(
                    name = if (category == allLabel) allLabel else LocalizationUtils.getLocalizedCategory(context, category),
                    icon = icon,
                    count = count,
                    isSelected = isSelected,
                    onClick = { selectedCategory = category }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (filteredTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_transactions), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 80.dp)
            ) {
                // Gunakan stickyHeader + item individual per transaksi
                // untuk virtualisasi LazyColumn yang benar (tidak render semua item sekaligus)
                groupedTransactions.forEach { (date, transactionsForDate) ->
                    stickyHeader(key = "header_$date") {
                        // Background agar header tidak transparan saat scroll
                        Surface(color = MaterialTheme.colorScheme.background) {
                            Text(
                                text = date,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, top = 20.dp, bottom = 8.dp)
                            )
                        }
                    }
                    
                    itemsIndexed(
                        items = transactionsForDate,
                        key = { _, tx -> tx.id }
                    ) { index, transaction ->
                        val isFirst = index == 0
                        val isLast = index == transactionsForDate.size - 1
                        val shape = RoundedCornerShape(
                            topStart = if (isFirst) 24.dp else 0.dp,
                            topEnd = if (isFirst) 24.dp else 0.dp,
                            bottomStart = if (isLast) 24.dp else 0.dp,
                            bottomEnd = if (isLast) 24.dp else 0.dp
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTransactionClick(transaction) },
                            shape = shape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isFirst || isLast) cardBorder(darkWidth = 1.2.dp) else null
                        ) {
                            Column {
                                TransactionRow(transaction, settings.language, showCard = false)
                                if (!isLast) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
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
