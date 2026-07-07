package com.appfira.financeai.logic

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.appfira.financeai.BuildConfig
import com.appfira.financeai.model.Transaction
import com.appfira.financeai.model.TransactionType
import com.appfira.financeai.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {
    
    fun exportToExcel(context: Context, transactions: List<Transaction>, language: com.appfira.financeai.model.Language) {
        // Since we can't easily add heavy dependencies like Apache POI in this environment setup without a build.gradle
        // I will implement a CSV-based export which is natively readable by Excel/Google Sheets
        // OR I can write a simple HTML-based Excel file which Excel opens perfectly.
        
        val filePrefix = if (language == com.appfira.financeai.model.Language.ENGLISH) "Financial_Report" else "Laporan_Keuangan"
        val fileName = "${filePrefix}_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        val locale = when (language) {
            com.appfira.financeai.model.Language.INDONESIAN -> Locale("id", "ID")
            com.appfira.financeai.model.Language.ENGLISH -> Locale.US
            com.appfira.financeai.model.Language.SYSTEM -> Locale.getDefault()
        }
        val dateFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_FULL, locale)
        
        try {
            file.outputStream().use { out ->
                // Add UTF-8 BOM for Excel compatibility
                out.write(0xEF)
                out.write(0xBB)
                out.write(0xBF)
                
                out.bufferedWriter().use { writer ->
                    // Header
                    val header = listOf(
                        context.getString(R.string.export_header_date),
                        context.getString(R.string.export_header_desc),
                        context.getString(R.string.export_header_category),
                        context.getString(R.string.export_header_type),
                        context.getString(R.string.export_header_amount)
                    ).joinToString(",") { escapeCsv(it) }
                    
                    writer.write("$header\n")
                    
                    val currencySymbol = context.getString(R.string.currency_symbol)
                    
                    // Data and Summary calculation in single pass if possible, 
                    // but for clarity and since transactions is already a list, 
                    // just avoid redundant filters.
                    var totalIncome = 0L
                    var totalExpense = 0L
                    
                    transactions.forEach { t ->
                        val isIncome = t.type == TransactionType.INCOME
                        if (isIncome) totalIncome += t.amount else totalExpense += t.amount
                        
                        val typeStr = if (isIncome) context.getString(R.string.type_income_label) else context.getString(R.string.type_expense_label)
                        val dateStr = dateFormat.format(Date(t.date))
                        val formattedAmount = "${currencySymbol} ${LocalizationUtils.formatCurrency(t.amount, language)}"
                        val row = listOf(
                            dateStr,
                            LocalizationUtils.getLocalizedDescription(context, t.description),
                            LocalizationUtils.getLocalizedCategory(context, t.category),
                            typeStr,
                            formattedAmount
                        ).joinToString(",") { escapeCsv(it) }
                        writer.write("$row\n")
                    }
                    
                    writer.write("\n\n${escapeCsv(context.getString(R.string.export_summary))}\n")
                    writer.write("${escapeCsv(context.getString(R.string.export_total_income))},${currencySymbol} ${LocalizationUtils.formatCurrency(totalIncome, language)}\n")
                    writer.write("${escapeCsv(context.getString(R.string.export_total_expense))},${currencySymbol} ${LocalizationUtils.formatCurrency(totalExpense, language)}\n")
                    writer.write("${escapeCsv(context.getString(R.string.export_final_balance))},${currencySymbol} ${LocalizationUtils.formatCurrency(totalIncome - totalExpense, language)}\n")
                    
                    writer.flush()
                }
            }
            
            shareFile(context, file)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }

    private fun shareFile(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooser = Intent.createChooser(intent, context.getString(R.string.export_chooser_title))
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("ExcelExporter", "Error sharing file", e)
        }
    }
}
