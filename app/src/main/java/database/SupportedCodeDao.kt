package database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SupportedCodeDao {
    @Insert
    fun insertSupportedCode(code: SupportedCode)

    @Delete
    fun deleteSupportedCode(code: SupportedCode)

    @Query("SELECT * FROM supported_codes")
    fun getAllSupportedCodes(): List<SupportedCode>

    @Query("DELETE FROM supported_codes")
    fun removeAllSupportedCodes()
}