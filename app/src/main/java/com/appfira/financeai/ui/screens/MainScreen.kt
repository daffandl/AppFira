package com.appfira.financeai.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.appfira.financeai.FinanceViewModel
import com.appfira.financeai.R
import com.appfira.financeai.logic.ExcelExporter
import com.appfira.financeai.logic.GoogleSheetsSync
import com.appfira.financeai.model.AppSettings
import com.appfira.financeai.ui.components.ChatInterface
import com.appfira.financeai.ui.components.Dashboard
import com.appfira.financeai.ui.navigation.FloatingNavigationBar

@Composable
fun MainScreen(
    viewModel: FinanceViewModel,
    settings: AppSettings,
    onLogout: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val income by viewModel.totalIncome.collectAsState(initial = 0L)
    val expense by viewModel.totalExpense.collectAsState(initial = 0L)
    val messages by viewModel.messages.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabHistory = remember { mutableStateListOf<Int>() }
    var showBottomBar by remember { mutableStateOf(true) }
    var selectedTransaction by remember { mutableStateOf<com.appfira.financeai.model.Transaction?>(null) }

    // Navigation back stack logic
    BackHandler(enabled = selectedTransaction != null || selectedTab != 0 || tabHistory.isNotEmpty()) {
        if (selectedTransaction != null) {
            selectedTransaction = null
        } else if (tabHistory.isNotEmpty()) {
            val previousTab = tabHistory.removeAt(tabHistory.size - 1)
            selectedTab = previousTab
        } else {
            selectedTab = 0
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FloatingNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        if (selectedTab != index) {
                            // Add current tab to history if it's different from the new index
                            // To prevent duplicate consecutive tabs in history
                            if (tabHistory.isEmpty() || tabHistory.last() != selectedTab) {
                                tabHistory.add(selectedTab)
                            }
                            selectedTab = index
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            
            when (selectedTab) {
                0 -> {
                    showBottomBar = true
                    Column {
                        Dashboard(income ?: 0L, expense ?: 0L, transactions, settings)
                        Box(modifier = Modifier.weight(1f)) {
                            ChatInterface(
                                messages = messages,
                                isLoading = isLoading,
                                paddingValues = paddingValues,
                                onSendMessage = { text, type -> viewModel.processInput(text, type) }
                            )
                        }
                    }
                }
                1 -> {
                    showBottomBar = selectedTransaction == null
                    if (selectedTransaction != null) {
                        TransactionDetailScreen(
                            transaction = selectedTransaction!!,
                            settings = settings,
                            onBack = { selectedTransaction = null },
                            onUpdate = { 
                                viewModel.updateTransaction(it)
                                selectedTransaction = null
                            },
                            onDelete = {
                                viewModel.deleteTransaction(selectedTransaction!!)
                                selectedTransaction = null
                            }
                        )
                    } else {
                        Column {
                            HistoryScreen(
                                transactions = transactions,
                                settings = settings,
                                paddingValues = paddingValues,
                                onExport = { 
                                    ExcelExporter.exportToExcel(context, transactions, settings.language)
                                },
                                onTransactionClick = { selectedTransaction = it }
                            )
                        }
                    }
                }
                2 -> {
                    showBottomBar = true
                    Column {
                        SpreadsheetScreen(
                            settings = settings,
                            paddingValues = paddingValues,
                            onCreateNew = { viewModel.createNewSpreadsheet(it) },
                            onSelectActive = { viewModel.setActiveSpreadsheet(it) },
                            onDelete = { viewModel.deleteSpreadsheet(it) },
                            onDeleteFromDrive = { viewModel.deleteSpreadsheetFromDrive(it) },
                            onToggleSync = { viewModel.toggleSync(it) },
                            onOpenLink = { id ->
                                val url = GoogleSheetsSync.getShareableLink(id)
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            },
                            onShareLink = { id ->
                                val url = GoogleSheetsSync.getShareableLink(id)
                                val shareMsg = context.getString(R.string.share_message, url)
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareMsg)
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, context.getString(R.string.share_label)).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            }
                        )
                    }
                }
                3 -> {
                    SettingsScreen(
                        userEmail = userEmail,
                        userName = userName,
                        userPhotoUrl = userPhotoUrl,
                        settings = settings,
                        paddingValues = paddingValues,
                        onLogout = onLogout,
                        onToggleSync = { viewModel.toggleSync(it) },
                        onSetLanguage = { viewModel.setLanguage(it) },
                        onSetTheme = { viewModel.setTheme(it) },
                        onSetAppIcon = { icon, immediate -> viewModel.setAppIcon(icon, immediate) },
                        onTogglePersonal = { viewModel.togglePersonalSync(it) },
                        onToggleShared = { viewModel.toggleSharedSync(it) },
                        onShowBottomBar = { showBottomBar = it }
                    )
                }
            }
        }
    }
}
