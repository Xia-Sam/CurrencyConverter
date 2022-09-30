package database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supported_codes")
data class SupportedCode(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: String,
    val currency: String
)
