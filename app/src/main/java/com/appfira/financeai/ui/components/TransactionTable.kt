package com.appfira.financeai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appfira.financeai.model.Transaction
import com.appfira.financeai.model.TransactionType
import com.appfira.financeai.ui.theme.Green
import com.appfira.financeai.ui.theme.Red
import com.appfira.financeai.logic.AppConstants
import com.appfira.financeai.R
import androidx.compose.ui.res.stringResource

import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import com.appfira.financeai.ui.theme.cardBorder

@Composable
fun TransactionRow(transaction: Transaction, language: com.appfira.financeai.model.Language, showCard: Boolean = true) {
    val locale = when (language) {
        com.appfira.financeai.model.Language.INDONESIAN -> Locale("id", "ID")
        com.appfira.financeai.model.Language.ENGLISH -> Locale.US
        com.appfira.financeai.model.Language.SYSTEM -> Locale.getDefault()
    }
    val dateFormat = remember(locale) { SimpleDateFormat(AppConstants.DATE_FORMAT_TIME, locale) }
    
    val categoryInfo = getCategoryInfo(transaction.category)
    val context = androidx.compose.ui.platform.LocalContext.current
    val localizedCategory = com.appfira.financeai.logic.LocalizationUtils.getLocalizedCategory(context, transaction.category)
    val displayDescription = com.appfira.financeai.logic.LocalizationUtils.getLocalizedDescription(context, transaction.description)

    val content = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryInfo.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryInfo.icon,
                    contentDescription = null,
                    tint = categoryInfo.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(displayDescription, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localizedCategory, color = categoryInfo.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(" • ", color = Color.Gray.copy(alpha = 0.5f), fontSize = 10.sp)
                    Text(dateFormat.format(Date(transaction.date)), color = Color.Gray, fontSize = 10.sp)
                }
            }

            val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
            val amountColor = if (transaction.type == TransactionType.INCOME) Green else Red
            
            val formattedAmount = stringResource(
                R.string.currency_format,
                stringResource(R.string.currency_symbol),
                com.appfira.financeai.logic.LocalizationUtils.formatCurrency(transaction.amount, language)
            )

            Text(
                "$prefix $formattedAmount",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }

    if (showCard) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = cardBorder(darkWidth = 1.2.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            content()
        }
    } else {
        content()
    }
}

data class CategoryUI(val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

@Composable
fun getCategoryInfo(category: String): CategoryUI {
    return when (category) {
        AppConstants.CAT_FOOD -> CategoryUI(Icons.Default.Restaurant, Color(0xFFF59E0B))
        AppConstants.CAT_TRANSPORT -> CategoryUI(Icons.Default.DirectionsCar, Color(0xFF3B82F6))
        AppConstants.CAT_ENTERTAINMENT -> CategoryUI(Icons.Default.Movie, Color(0xFF8B5CF6))
        AppConstants.CAT_SHOPPING -> CategoryUI(Icons.Default.ShoppingBag, Color(0xFFEC4899))
        AppConstants.CAT_HEALTH -> CategoryUI(Icons.Default.MedicalServices, Color(0xFF10B981))
        AppConstants.CAT_EDUCATION -> CategoryUI(Icons.Default.School, Color(0xFF6366F1))
        AppConstants.CAT_SALARY -> CategoryUI(Icons.Default.Payments, Color(0xFF10B981))
        AppConstants.CAT_INVESTMENT -> CategoryUI(Icons.Default.TrendingUp, Color(0xFF06B6D4))
        AppConstants.CAT_BILL -> CategoryUI(Icons.Default.Receipt, Color(0xFF6B7280))
        AppConstants.CAT_BONUS -> CategoryUI(Icons.Default.CardGiftcard, Color(0xFF8B5CF6))
        AppConstants.CAT_HOME -> CategoryUI(Icons.Default.Home, Color(0xFF10B981))
        AppConstants.CAT_TRAVEL -> CategoryUI(Icons.Default.Flight, Color(0xFF3B82F6))
        AppConstants.CAT_CLOTHING -> CategoryUI(Icons.Default.Checkroom, Color(0xFFEC4899))
        else -> CategoryUI(Icons.Default.Category, Color(0xFF6B7280))
    }
}
