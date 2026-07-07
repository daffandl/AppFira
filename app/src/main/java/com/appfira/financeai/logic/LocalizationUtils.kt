package com.appfira.financeai.logic

import android.content.Context
import android.content.res.Configuration
import com.appfira.financeai.model.Language
import java.util.*

object LocalizationUtils {
    fun setLocale(context: Context, language: Language): Context {
        if (language == Language.SYSTEM) return context
        
        val locale = when (language) {
            Language.SYSTEM -> Locale.getDefault()
            Language.INDONESIAN -> Locale("id", "ID")
            Language.ENGLISH -> Locale("en", "US")
        }
        
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }

    fun getLocalizedCategory(context: Context, categoryKey: String): String {
        val resId = context.resources.getIdentifier(categoryKey, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else categoryKey
    }

    fun getLocalizedDescription(context: Context, description: String): String {
        // DESC_INCOME_DEFAULT = "DEFAULT_INCOME", DESC_EXPENSE_DEFAULT = "DEFAULT_EXPENSE"
        return when (description.uppercase(Locale.ROOT)) {
            AppConstants.DESC_INCOME_DEFAULT ->
                context.getString(com.appfira.financeai.R.string.type_income_label)
            AppConstants.DESC_EXPENSE_DEFAULT ->
                context.getString(com.appfira.financeai.R.string.type_expense_label)
            else -> description
        }
    }

    fun formatCurrency(amount: Long, language: Language): String {
        val locale = when (language) {
            Language.SYSTEM -> Locale.getDefault()
            Language.INDONESIAN -> Locale("id", "ID")
            Language.ENGLISH -> Locale.US
        }
        val formatter = java.text.NumberFormat.getNumberInstance(locale)
        return formatter.format(amount)
    }
}
