package com.appfira.financeai.logic

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.appfira.financeai.model.AppIcon

object Utils {
    fun changeAppIcon(context: Context, icon: AppIcon, immediate: Boolean = false) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        val components = listOf(
            "$packageName.MainActivityDefault",
            "$packageName.MainActivityDark",
            "$packageName.MainActivityThemed"
        )

        val activeComponent = when (icon) {
            AppIcon.DEFAULT -> "$packageName.MainActivityDefault"
            AppIcon.DARK -> "$packageName.MainActivityDark"
            AppIcon.THEMED -> "$packageName.MainActivityThemed"
        }

        components.forEach { component ->
            val state = if (component == activeComponent) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            val flags = if (immediate) 0 else PackageManager.DONT_KILL_APP

            packageManager.setComponentEnabledSetting(
                ComponentName(context, component),
                state,
                flags
            )
        }
    }
}
