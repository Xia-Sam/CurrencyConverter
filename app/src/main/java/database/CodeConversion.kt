package database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "code_conversions")
data class CodeConversion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val base_code: String,
    val target_code: String,
    val rate: Float,
    val last_updated_time: String
)
