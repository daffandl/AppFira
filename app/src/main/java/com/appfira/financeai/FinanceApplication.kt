package com.appfira.financeai

import android.app.Application
import com.appfira.financeai.di.appModule
import com.appfira.financeai.di.dataModule
import com.appfira.financeai.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FinanceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@FinanceApplication)
            modules(listOf(dataModule, domainModule, appModule))
        }
    }
}
