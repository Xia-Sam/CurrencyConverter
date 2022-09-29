package com.example.cconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.cconverter.databinding.ActivityMainBinding
import datasource.ExtrangeRateApi
import datasource.PairConversion
import datasource.SupportedCodes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    val extrangeRateApi = ExtrangeRateApi.create()
    lateinit var supportedCodes: List<List<String>>
    private var baseCode = "AED"
    private var targetCode = "AED"
    private var amount = 1.0f
    private val clickListener = View.OnClickListener {
        when (it.id) {
            R.id.button_convert -> showResult()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val call = extrangeRateApi.getSupportedCodes()
        call.enqueue(object : Callback<SupportedCodes> {
            override fun onResponse(
                call: Call<SupportedCodes>,
                response: Response<SupportedCodes>
            ) {
                if (response.isSuccessful and (response.body()!!.result == "success")) {
                    supportedCodes = response.body()!!.supported_codes
                    Log.d(TAG, "supported code obtained successfully")

                    val arrayAdapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        supportedCodes
                    )
                    arrayAdapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    with (binding.toCurrency) {
                        adapter = arrayAdapter
                        setSelection(0, false)
                        onItemSelectedListener = this@MainActivity
                    }
                    with (binding.fromCurrency) {
                        adapter = arrayAdapter
                        setSelection(0, false)
                        onItemSelectedListener = this@MainActivity
                    }
                }
            }

            override fun onFailure(call: Call<SupportedCodes>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
            }

        })

        binding.buttonConvert.setOnClickListener(clickListener)
    }

    private fun showResult() {
        amount = binding.amount.text.toString().toFloat()
        val call = extrangeRateApi.getConversionResult(baseCode, targetCode, amount)
        Log.d(TAG, "Conversion result requested: " +
                "baseCode: $baseCode, targetCode: $targetCode, amount: $amount")
        call.enqueue(object : Callback<PairConversion> {
            override fun onResponse(
                call: Call<PairConversion>,
                response: Response<PairConversion>
            ) {
                if (response.isSuccessful and (response.body()!!.result == "success")) {
                    val conversionResult = response.body()!!.conversion_result
                    binding.afterAmount.text = conversionResult.toString()
                }
            }

            override fun onFailure(call: Call<PairConversion>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
            }

        })

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent) {
            binding.fromCurrency -> {
                baseCode = supportedCodes[position][0]
                Log.d(TAG, "base code changed to $baseCode, position=$position")
            }
            binding.toCurrency -> {
                targetCode = supportedCodes[position][0]
                Log.d(TAG, "target code changed to $targetCode, position=$position")
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}