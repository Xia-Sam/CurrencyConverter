package database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CodeConversionDao {
    @Insert
    fun insertCodeConversion(code: CodeConversion)

    @Delete
    fun deleteCodeConversion(code: CodeConversion)

    @Query("SELECT * FROM code_conversions " +
            "WHERE base_code= :baseCode AND target_code= :targetCode")
    fun getCodeConversion(baseCode: String, targetCode: String): CodeConversion?

    @Query("UPDATE code_conversions " +
            "SET rate= :newRate, last_updated_time= :newTime " +
            "WHERE base_code= :baseCode AND target_code= :targetCode")
    fun update(baseCode: String, targetCode: String, newRate: Float, newTime: String)

    @Query("SELECT * FROM code_conversions")
    fun getAllCodeConversions(): List<CodeConversion>

    @Query("DELETE FROM code_conversions")
    fun removeAllCodeConversions()
}