package com.example.cconverter

import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.cconverter.databinding.ActivityMainBinding
import database.DataSource
import database.SupportedCode
import database.SupportedCodeDao
import datasource.ExtrangeRateApi
import datasource.PairConversion
import datasource.SupportedCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import network.NetworkMonitor

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
    private val networkMonitor = NetworkMonitor()
    private lateinit var dataBase: SupportedCodeDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkMonitor.init(this)
        dataBase = DataSource.getDao(this)

        try {
            lifecycleScope.launch(Dispatchers.IO) {
                if (networkMonitor.isNetworkAvailable()) {
                    // Update local database
                    val response = extrangeRateApi.getSupportedCodes().execute()
                    supportedCodes = response.body()!!.supported_codes
                    dataBase.removeAllSupportedCodes()
                    for (codeList in supportedCodes) {
                        dataBase.insertSupportedCode(SupportedCode(0, codeList[0], codeList[1]))
                    }
                } else {
                    // Get supported codes from local database
                    val l = mutableListOf<List<String>>()
                    for (scode in dataBase.getAllSupportedCodes()) {
                        l.add(listOf(scode.code, scode.currency))
                    }
                    supportedCodes = l
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    val arrayAdapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        supportedCodes
                    )
                    arrayAdapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    with(binding.toCurrency) {
                        adapter = arrayAdapter
                        setSelection(0, false)
                        onItemSelectedListener = this@MainActivity
                    }
                    with(binding.fromCurrency) {
                        adapter = arrayAdapter
                        setSelection(0, false)
                        onItemSelectedListener = this@MainActivity
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.buttonConvert.setOnClickListener(clickListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkMonitor.finish()
    }

    private fun showResult() {
        amount = binding.amount.text.toString().toFloat()
        val call = extrangeRateApi.getConversionResult(baseCode, targetCode, amount)
        Log.d(
            TAG, "Conversion result requested: " +
                    "baseCode: $baseCode, targetCode: $targetCode, amount: $amount"
        )
        call.enqueue(object : Callback<PairConversion> {
            override fun onResponse(
                call: Call<PairConversion>,
                response: Response<PairConversion>
            ) {
                if (response.body() == null) {
                    Utils.showToast(this@MainActivity, "API key is invalid")
                } else {
                    binding.timeLastUpdated.text = response.body()?.time_last_update_utc
                    binding.afterAmount.text = response.body()?.conversion_result.toString()
                }
            }

            override fun onFailure(call: Call<PairConversion>, t: Throwable) {
                Utils.showToast(
                    this@MainActivity,
                    "Failed to call API, error: " + "${t.message}"
                )
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