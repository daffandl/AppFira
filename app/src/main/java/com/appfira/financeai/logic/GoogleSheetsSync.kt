package com.appfira.financeai.logic

import android.content.Context
import android.util.Log
import com.appfira.financeai.BuildConfig
import com.appfira.financeai.model.Transaction
import com.appfira.financeai.model.TransactionType
import com.appfira.financeai.R
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object GoogleSheetsSync {
    private const val TAG = "GoogleSheetsSync"
    private const val FOLDER_NAME = AppConstants.DRIVE_FOLDER_NAME
    private val SCOPES = listOf(
        "https://www.googleapis.com/auth/spreadsheets",
        "https://www.googleapis.com/auth/drive.file"
    )

    private val transport by lazy { GoogleNetHttpTransport.newTrustedTransport() }
    private val jsonFactory by lazy { GsonFactory.getDefaultInstance() }

    private fun getSheetsService(context: Context, accountName: String): Sheets {
        val credential = GoogleAccountCredential.usingOAuth2(context.applicationContext, SCOPES)
        credential.selectedAccountName = accountName

        return Sheets.Builder(
            transport,
            jsonFactory,
            credential
        ).setApplicationName("Finance AI").build()
    }

    private fun getDriveService(context: Context, accountName: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(context.applicationContext, SCOPES)
        credential.selectedAccountName = accountName

        return Drive.Builder(
            transport,
            jsonFactory,
            credential
        ).setApplicationName("Finance AI").build()
    }

    private suspend fun getOrCreateFolder(driveService: Drive): String? = withContext(Dispatchers.IO) {
        try {
            val query = "name = '$FOLDER_NAME' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
            val result = driveService.files().list().setQ(query).setSpaces("drive").execute()
            val folders = result.files
            
            if (!folders.isNullOrEmpty()) {
                return@withContext folders[0].id
            }

            val folderMetadata = File()
            folderMetadata.name = FOLDER_NAME
            folderMetadata.mimeType = "application/vnd.google-apps.folder"

            val folder = driveService.files().create(folderMetadata).setFields("id").execute()
            folder.id
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating folder", e)
            null
        }
    }

    suspend fun createSpreadsheet(context: Context, accountName: String, title: String = "Finance AI Tracker", isPublic: Boolean = false): String? = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(context, accountName)
            val folderId = getOrCreateFolder(driveService)

            val sheetsService = getSheetsService(context, accountName)
            val spreadsheet = Spreadsheet().setProperties(
                SpreadsheetProperties().setTitle(title)
            )
            val result = sheetsService.spreadsheets().create(spreadsheet).execute()
            val spreadsheetId = result.spreadsheetId

            // Move to folder if created
            if (folderId != null) {
                val file = driveService.files().get(spreadsheetId).setFields("parents").execute()
                val previousParents = file.parents?.joinToString(",")
                driveService.files().update(spreadsheetId, null)
                    .setAddParents(folderId)
                    .setRemoveParents(previousParents)
                    .execute()
            }

            // Set Public Permission if requested
            if (isPublic) {
                val permission = Permission()
                permission.type = "anyone"
                permission.role = "reader"
                driveService.permissions().create(spreadsheetId, permission).execute()
            }

            // Initial Header
            val header = listOf(
                context.getString(R.string.export_header_date),
                context.getString(R.string.export_header_desc),
                context.getString(R.string.export_header_category),
                context.getString(R.string.export_header_type),
                context.getString(R.string.export_header_amount)
            )
            val body = ValueRange().setValues(listOf(header))
            sheetsService.spreadsheets().values().append(spreadsheetId, "Sheet1!A1", body)
                .setValueInputOption("USER_ENTERED").execute()
            
            spreadsheetId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating spreadsheet", e)
            null
        }
    }

    suspend fun syncTransaction(
        context: Context,
        transaction: Transaction,
        spreadsheetId: String,
        accountName: String
    ): Boolean = withContext(Dispatchers.IO) {
        val dateStr = SimpleDateFormat(AppConstants.DATE_FORMAT_FULL, Locale.getDefault()).format(Date(transaction.date))
        val typeStr = if (transaction.type == TransactionType.INCOME) 
            context.getString(R.string.type_income_label) 
        else 
            context.getString(R.string.type_expense_label)
        
        val rowData = listOf(
            dateStr,
            LocalizationUtils.getLocalizedDescription(context, transaction.description),
            LocalizationUtils.getLocalizedCategory(context, transaction.category),
            typeStr,
            transaction.amount.toString()
        )

        try {
            val service = getSheetsService(context, accountName)
            val body = ValueRange().setValues(listOf(rowData))
            
            service.spreadsheets().values().append(spreadsheetId, "Sheet1!A1", body)
                .setValueInputOption("USER_ENTERED")
                .execute()
            
            if (BuildConfig.DEBUG) Log.d(TAG, "Successfully synced to Google Sheets: $rowData")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Google Sheets", e)
            false
        }
    }

    suspend fun syncTransactions(
        context: Context,
        transactions: List<Transaction>,
        spreadsheetId: String,
        accountName: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (transactions.isEmpty()) return@withContext true

        val dataRows = transactions.map { transaction ->
            val dateStr = SimpleDateFormat(AppConstants.DATE_FORMAT_FULL, Locale.getDefault()).format(Date(transaction.date))
            val typeStr = if (transaction.type == TransactionType.INCOME) 
                context.getString(R.string.type_income_label) 
            else 
                context.getString(R.string.type_expense_label)
                
            listOf(
                dateStr,
                LocalizationUtils.getLocalizedDescription(context, transaction.description),
                LocalizationUtils.getLocalizedCategory(context, transaction.category),
                typeStr,
                transaction.amount.toString()
            )
        }

        try {
            val service = getSheetsService(context, accountName)
            val body = ValueRange().setValues(dataRows)
            
            service.spreadsheets().values().append(spreadsheetId, "Sheet1!A1", body)
                .setValueInputOption("USER_ENTERED")
                .execute()
            
            if (BuildConfig.DEBUG) Log.d(TAG, "Successfully synced ${transactions.size} transactions to Google Sheets")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing transactions to Google Sheets", e)
            false
        }
    }

    suspend fun rewriteTransactions(
        context: Context,
        transactions: List<Transaction>,
        spreadsheetId: String,
        accountName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getSheetsService(context, accountName)
            // Clear existing rows (starting from row 2 to keep headers)
            service.spreadsheets().values().clear(spreadsheetId, "Sheet1!A2:Z", ClearValuesRequest()).execute()
            
            // Sync all transactions again; propagate sukses/gagal ke caller
            if (transactions.isNotEmpty()) {
                syncTransactions(context, transactions, spreadsheetId, accountName)
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rewriting transactions to Google Sheets", e)
            false
        }
    }

    fun getShareableLink(spreadsheetId: String): String {
        return "https://docs.google.com/spreadsheets/d/$spreadsheetId/edit?usp=sharing"
    }

    suspend fun listSpreadsheetsInFolder(context: Context, accountName: String): List<File> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(context, accountName)
            val folderId = getOrCreateFolder(driveService) ?: return@withContext emptyList()

            val query = "'$folderId' in parents and mimeType = 'application/vnd.google-apps.spreadsheet' and trashed = false"
            val result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name, createdTime)")
                .execute()

            result.files ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error listing spreadsheets in folder", e)
            emptyList()
        }
    }

    suspend fun deleteSpreadsheet(context: Context, accountName: String, spreadsheetId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(context, accountName)
            driveService.files().delete(spreadsheetId).execute()
            if (BuildConfig.DEBUG) Log.d(TAG, "Successfully deleted spreadsheet: $spreadsheetId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting spreadsheet: $spreadsheetId", e)
            false
        }
    }

    /**
     * Clears all data rows in the spreadsheet (A2:Z) while keeping the header row intact.
     * Used for "Delete All Transactions" without removing the spreadsheet file.
     */
    suspend fun clearTransactionRows(
        context: Context,
        spreadsheetId: String,
        accountName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getSheetsService(context, accountName)
            service.spreadsheets().values()
                .clear(spreadsheetId, "Sheet1!A2:Z", ClearValuesRequest())
                .execute()
            if (BuildConfig.DEBUG) Log.d(TAG, "Successfully cleared transaction rows from sheet: $spreadsheetId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing transaction rows from sheet: $spreadsheetId", e)
            false
        }
    }
}
