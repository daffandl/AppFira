package com.appfira.financeai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import android.app.Activity
import com.appfira.financeai.R
import com.appfira.financeai.logic.AppConstants
import com.appfira.financeai.model.AppSettings
import com.appfira.financeai.model.Language
import com.appfira.financeai.model.Transaction
import com.appfira.financeai.model.TransactionType
import com.appfira.financeai.ui.theme.*
import com.appfira.financeai.ui.theme.cardBorder
import java.util.Calendar

@Composable
fun Dashboard(income: Long, expense: Long, transactions: List<Transaction>, settings: AppSettings) {
    val balance = income - expense
    
    val view = LocalView.current
    val darkTheme = LocalDarkTheme.current
    
    if (!view.isInEditMode) {
        // Force white icons while dashboard is active
        SideEffect {
            findActivity(view.context)?.let { activity ->
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
            }
        }

        // Restore based on theme when leaving
        DisposableEffect(darkTheme) {
            onDispose {
                findActivity(view.context)?.let { activity ->
                    val window = activity.window
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    // Restore theme preference: dark icons for light theme, light icons for dark theme
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                }
            }
        }
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(LightBlue, DeepBlue)
    )

    val points = remember(transactions) {
        if (transactions.isEmpty()) return@remember listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val today = calendar.timeInMillis
        val sixDaysAgo = today - 6 * 24 * 60 * 60 * 1000L
        
        // Group by day in a single pass
        val dailyNet = mutableMapOf<Long, Long>()
        var totalBefore = 0L
        
        transactions.forEach { t ->
            if (t.date < sixDaysAgo) {
                totalBefore += if (t.type == TransactionType.INCOME) t.amount else -t.amount
            } else {
                val dayStart = today - ((today - t.date) / (24 * 60 * 60 * 1000L)) * (24 * 60 * 60 * 1000L)
                val current = dailyNet.getOrDefault(dayStart, 0L)
                dailyNet[dayStart] = current + (if (t.type == TransactionType.INCOME) t.amount else -t.amount)
            }
        }
        
        var currentBalance = totalBefore
        (0..6).map { day ->
            val dayStart = sixDaysAgo + day * 24 * 60 * 60 * 1000L
            currentBalance += dailyNet.getOrDefault(dayStart, 0L)
            currentBalance.toFloat()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Canvas(
            modifier = Modifier.matchParentSize() 
        ) {
            val w = size.width
            val h = size.height
            val cornerRadius = 40.dp.toPx() 

            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, h)
                quadraticBezierTo(
                    0f, h - cornerRadius,
                    cornerRadius, h - cornerRadius 
                )
                lineTo(w - cornerRadius, h - cornerRadius) 
                quadraticBezierTo(
                    w, h - cornerRadius, 
                    w, h 
                )
                lineTo(w, 0f)
                close() 
            }
            
            drawPath(
                path = path,
                brush = gradientBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 48.dp) 
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(DeepBlue, Color(0xFF0369A1))
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        SimpleTrendGraph(points, Color.White.copy(alpha = 0.35f))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_wallet),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.total_balance),
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val formattedBalance = stringResource(
                            R.string.currency_format,
                            stringResource(R.string.currency_symbol),
                            com.appfira.financeai.logic.LocalizationUtils.formatCurrency(balance, settings.language)
                        )
                        Text(
                            text = formattedBalance,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    label = stringResource(R.string.income),
                    amount = income,
                    color = Green,
                    iconResId = R.drawable.ic_income,
                    language = settings.language,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    label = stringResource(R.string.expense),
                    amount = expense,
                    color = Red,
                    iconResId = R.drawable.ic_expense,
                    language = settings.language,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SimpleTrendGraph(points: List<Float>, color: Color) {
    if (points.isEmpty()) return

    // Pre-compute paths hanya saat points berubah — bukan setiap frame/recompose
    val pathPair = remember(points) {
        val linePath = Path()
        val fill = Path()

        // Normalisasi nilai
        val maxPoint = points.maxOrNull() ?: 1f
        val minPoint = points.minOrNull() ?: 0f
        val range = if (maxPoint == minPoint) 1f else maxPoint - minPoint

        // Simpan titik normalized (0..1) untuk di-scale saat draw
        val normalized = points.map { (it - minPoint) / range }
        Pair(normalized, range > 0f)
    }

    val normalizedPoints = pathPair.first

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val linePath = Path()
        val fillPath = Path()

        val stepX = if (normalizedPoints.size > 1) width / (normalizedPoints.size - 1).toFloat() else width

        normalizedPoints.forEachIndexed { index, norm ->
            val x = index * stepX
            val y = height - (norm * height * 0.6f + height * 0.1f)
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * stepX
                val prevNorm = normalizedPoints[index - 1]
                val prevY = height - (prevNorm * height * 0.6f + height * 0.1f)
                linePath.cubicTo(prevX + stepX / 2, prevY, x - stepX / 2, y, x, y)
                fillPath.cubicTo(prevX + stepX / 2, prevY, x - stepX / 2, y, x, y)
            }
            if (index == normalizedPoints.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.25f), Color.Transparent)
            )
        )
        drawPath(path = linePath, color = color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun StatCard(label: String, amount: Long, color: Color, iconResId: Int, language: Language, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = cardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = iconResId), contentDescription = null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(6.dp))
            val formattedAmount = stringResource(
                R.string.currency_format,
                stringResource(R.string.currency_symbol),
                com.appfira.financeai.logic.LocalizationUtils.formatCurrency(amount, language)
            )
            Text(text = formattedAmount, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun findActivity(context: android.content.Context): Activity? {
    var currentContext = context
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}
