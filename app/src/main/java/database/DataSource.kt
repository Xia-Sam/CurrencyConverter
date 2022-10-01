package database

import android.content.Context
import androidx.room.Room

class DataSource {
    companion object {
        private var db : AppDatabase? = null
        private fun init(context: Context) {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "currency_code_database"
            ).build()
        }
        fun getSupportedCodeDao(context: Context): SupportedCodeDao {
            if (db == null) {
                init(context)
            }
            return db!!.supportedCodeDao()
        }

        fun getCodeConversionDao(context: Context): CodeConversionDao {
            if (db == null) {
                init(context)
            }
            return db!!.codeConversionDao()
        }
    }
}