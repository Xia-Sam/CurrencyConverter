package datasource

data class SupportedCodes(
    val result: String,
    val documentation: String,
    val terms_of_use: String,
    val supported_codes: List<List<String>>
)
