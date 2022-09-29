package datasource

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ExtrangeRateApi {
    companion object {
        private const val BASE_URL = "https://v6.exchangerate-api.com/"
        fun create(): ExtrangeRateApi {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ExtrangeRateApi::class.java)
        }
    }

    @GET("v6/d3388a1cdd64c0a51fbe2347/codes")
    fun getSupportedCodes(): Call<SupportedCodes>

    @GET("v6/d3388a1cdd64c0a51fbe2347/pair/{base}/{target}/{amount}")
    fun getConversionResult(
        @Path("base") baseCode: String,
        @Path("target") targetCode: String,
        @Path("amount") currencyAmount: Float
    ): Call<PairConversion>
}