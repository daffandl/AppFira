package com.appfira.financeai.data

import androidx.room.*
import com.appfira.financeai.model.Transaction
import com.appfira.financeai.model.SpreadsheetInfo
import com.appfira.financeai.model.TransactionType
import kotlinx.coroutines.flow.Flow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Long?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Long?>
}

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val spreadsheets: List<SpreadsheetInfo>,
    val activeSpreadsheetId: String?,
    val isSyncEnabled: Boolean,
    val language: String = "SYSTEM",
    val theme: String = "SYSTEM",
    val appIcon: String = "DEFAULT",
    val personalSpreadsheetId: String?,
    val sharedSpreadsheetId: String?,
    val isPersonalSyncEnabled: Boolean,
    val isSharedSyncEnabled: Boolean
)

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 0")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)
}

class Converters {
    @TypeConverter
    fun fromSpreadsheetInfoList(value: List<SpreadsheetInfo>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSpreadsheetInfoList(value: String): List<SpreadsheetInfo> {
        val listType = object : TypeToken<List<SpreadsheetInfo>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}

@Database(entities = [Transaction::class, SettingsEntity::class], version = 10, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN lastUpdated INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance-db"
                ).addMigrations(MIGRATION_9_10).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
