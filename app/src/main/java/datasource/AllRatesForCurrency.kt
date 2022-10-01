package datasource

data class AllRatesForCurrency(
    val result: String,
    val documentation: String,
    val terms_of_use: String,
    val time_last_update_unix: Int,
    val time_last_update_utc: String,
    val time_next_update_unix: Int,
    val time_next_update_utc: String,
    val base_code: String,
    val conversion_rates: Map<String, Float>
)
