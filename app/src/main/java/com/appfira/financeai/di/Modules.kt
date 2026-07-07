package com.appfira.financeai.di

import com.appfira.financeai.data.AppDatabase
import com.appfira.financeai.data.FinanceRepository
import com.appfira.financeai.FinanceViewModel
import com.appfira.financeai.domain.ManageSpreadsheetUseCase
import com.appfira.financeai.domain.ProcessChatUseCase
import com.appfira.financeai.domain.SyncSpreadsheetUseCase
import com.appfira.financeai.logic.GoogleSheetsSync
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { AppDatabase.getDatabase(androidApplication()) }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().settingsDao() }
    single { FinanceRepository(get(), get()) }
}

val domainModule = module {
    factory { ProcessChatUseCase(get()) }
    factory { SyncSpreadsheetUseCase(androidApplication(), get()) }
    factory { ManageSpreadsheetUseCase(androidApplication(), get()) }
}

val appModule = module {
    viewModel { 
        FinanceViewModel(
            application = androidApplication(), 
            repository = get(),
            processChatUseCase = get(),
            syncSpreadsheetUseCase = get(),
            manageSpreadsheetUseCase = get()
        ) 
    }
}
