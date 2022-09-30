package database

import android.content.Context
import androidx.room.Room

class DataSource {
    companion object {
        fun getDao(context: Context): SupportedCodeDao {
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "supported_codes_database"
            ).build()

            return db.supportedCodeDao()
        }
    }
}