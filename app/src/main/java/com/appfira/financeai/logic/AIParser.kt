package com.appfira.financeai.logic

import com.appfira.financeai.model.Transaction
import com.appfira.financeai.model.TransactionType
import java.util.*

object AIParser {
    private val expenseKeywords = AppConstants.EXPENSE_KEYWORDS
    private val incomeKeywords = AppConstants.INCOME_KEYWORDS
    private val categoryMap = AppConstants.CATEGORY_MAP

    private val amountRegex = Regex("""(\d+(?:[.,]\d+)?)\s*(rb|ribu|jt|juta|k)?""", RegexOption.IGNORE_CASE)
    private val extraSpacesRegex = Regex("""\s+""")

    fun parse(input: String, forcedType: TransactionType? = null): Transaction? {
        val lowerInput = input.lowercase(Locale.ROOT)
        
        // Extract amount
        val amount = extractAmount(lowerInput) ?: return null
        
        // Determine type: MUST use forcedType if provided
        val type = forcedType ?: if (incomeKeywords.any { lowerInput.contains(it) }) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }
        
        // Determine category
        var category = AppConstants.CAT_OTHER
        for ((key, value) in categoryMap) {
            if (lowerInput.contains(key)) {
                category = value
                break
            }
        }
        
        // Clean description — work entirely on lowercase to ensure consistent removal
        var description = lowerInput
        
        // Remove amount string (with suffixes like rb, jt, k)
        description = amountRegex.replace(description, "").trim()
        
        // Remove keywords (plain contains-based replace; \b doesn't work for Indonesian chars)
        val keywordsToRemove = (expenseKeywords + incomeKeywords)
            .distinct()
            .sortedByDescending { it.length } // remove longer patterns first
        for (kw in keywordsToRemove) {
            description = description.replace(kw, "", ignoreCase = true)
        }
        
        // Remove extra spaces
        var finalDescription = extraSpacesRegex.replace(description, " ").trim()

        if (finalDescription.isEmpty()) {
            finalDescription = if (type == TransactionType.INCOME) AppConstants.DESC_INCOME_DEFAULT else AppConstants.DESC_EXPENSE_DEFAULT
        }

        return Transaction(
            amount = amount,
            description = finalDescription.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            category = category,
            type = type
        )
    }

    private fun extractAmount(input: String): Long? {
        val match = amountRegex.find(input) ?: return null
        
        val rawValue = match.groupValues[1].replace(",", ".")
        val value = rawValue.toDoubleOrNull() ?: return null
        val suffix = match.groupValues[2].lowercase(Locale.ROOT)
        
        val multiplier = when (suffix) {
            "rb", "ribu", "k" -> 1000.0
            "jt", "juta" -> 1000000.0
            else -> 1.0
        }
        
        return (value * multiplier).toLong()
    }
}
