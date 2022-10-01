package database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SupportedCode::class, CodeConversion::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun supportedCodeDao(): SupportedCodeDao
    abstract fun codeConversionDao(): CodeConversionDao
}