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
import database.*
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
    private lateinit var baseCode: String
    private lateinit var targetCode: String
    private var amount = 1.0f
    private val clickListener = View.OnClickListener {
        when (it.id) {
            R.id.button_convert -> showResult()
        }
    }
    private val networkMonitor = NetworkMonitor()
    private lateinit var supportedCodeDataBase: SupportedCodeDao
    private lateinit var codeConversionDataBase: CodeConversionDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkMonitor.init(this)
        supportedCodeDataBase = DataSource.getSupportedCodeDao(this)
        codeConversionDataBase = DataSource.getCodeConversionDao(this)
        val sp = getSharedPreferences("cconverter", Context.MODE_PRIVATE).also {
            baseCode = it.getString("baseCode", "AED").toString()
            targetCode = it.getString("targetCode", "AED").toString()
            amount = it.getFloat("amount", 1.0f)
            binding.amount.setText(amount.toString())
            binding.timeLastUpdated.text = it.getString("lastUpdatedTime", "")
            binding.afterAmount.text = it.getString("afterAmount", "")
        }

        try {
            lifecycleScope.launch(Dispatchers.IO) {
                if (networkMonitor.isNetworkAvailable()) {
                    // Update local database
                    val response = extrangeRateApi.getSupportedCodes().execute()
                    supportedCodes = response.body()!!.supported_codes
                    supportedCodeDataBase.removeAllSupportedCodes()
                    for (codeList in supportedCodes) {
                        supportedCodeDataBase.insertSupportedCode(
                            SupportedCode(0, codeList[0], codeList[1])
                        )
                    }
                    Log.d(
                        TAG, "database updated successfully, " +
                                "codes in total: ${supportedCodes.size}"
                    )

                } else {
                    // Get supported codes from local database
                    val l = mutableListOf<List<String>>()
                    for (scode in supportedCodeDataBase.getAllSupportedCodes()) {
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
                        setSelection(sp.getInt("toCurrencyPos", 0), false)
                        onItemSelectedListener = this@MainActivity
                    }
                    with(binding.fromCurrency) {
                        adapter = arrayAdapter
                        setSelection(sp.getInt("fromCurrencyPos", 0), false)
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

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "ONSTOP, saving app state...")
        with (getSharedPreferences("cconverter", Context.MODE_PRIVATE).edit()) {
            putString("baseCode", baseCode)
            putString("targetCode", targetCode)
            putFloat("amount", amount)
            putString("lastUpdatedTime", binding.timeLastUpdated.text.toString())
            putString("afterAmount", binding.afterAmount.text.toString())
            apply()
        }
    }

    private fun showResult() {
        binding.timeLastUpdated.text = ""
        binding.afterAmount.text = ""

        amount = binding.amount.text.toString().toFloat()
        if (!networkMonitor.isNetworkAvailable()) {
            lifecycleScope.launch(Dispatchers.IO) {
                codeConversionDataBase.getCodeConversion(baseCode, targetCode)?.let {
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.timeLastUpdated.text = it.last_updated_time
                        binding.afterAmount.text = (amount * (it.rate)).toString()
                    }
                }
            }
            return
        }

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
                    val time = response.body()!!.time_last_update_utc
                    binding.timeLastUpdated.text = time
                    val result = response.body()!!.conversion_result
                    binding.afterAmount.text = result.toString()

                    lifecycleScope.launch(Dispatchers.IO) {
                        if (codeConversionDataBase.getCodeConversion(baseCode, targetCode)==null) {
                            codeConversionDataBase.insertCodeConversion(
                                CodeConversion(
                                    0,
                                    baseCode,
                                    targetCode,
                                    result/amount,
                                    time
                                )
                            )
                            Log.d(TAG, "one code conversion stored: $baseCode, $targetCode")
                        } else {
                            codeConversionDataBase.update(baseCode, targetCode, result/amount, time)
                            Log.d(TAG, "one code conversion updated: $baseCode, $targetCode")
                        }
                    }
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
        val editor = getSharedPreferences("cconverter", Context.MODE_PRIVATE).edit()
        when (parent) {
            binding.fromCurrency -> {
                baseCode = supportedCodes[position][0]
                editor.putInt("fromCurrencyPos", position)
                Log.d(TAG, "base code changed to $baseCode, position=$position")
            }
            binding.toCurrency -> {
                targetCode = supportedCodes[position][0]
                editor.putInt("toCurrencyPos", position)
                Log.d(TAG, "target code changed to $targetCode, position=$position")
            }
        }
        editor.apply()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}