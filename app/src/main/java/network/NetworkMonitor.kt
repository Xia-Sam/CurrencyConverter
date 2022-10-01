package network

import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class NetworkMonitor {
    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            GlobalScope.launch(Dispatchers.Main) {
                Utils.showToast(context, "Network connected")
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            GlobalScope.launch(Dispatchers.Main) {
                MaterialAlertDialogBuilder(context)
                    .setTitle("Network disconnected")
                    .setMessage("Go to network setting?")
                    .setPositiveButton("yes") { _, _ ->
                        startActivity(context, Intent(Settings.ACTION_WIRELESS_SETTINGS), null)
                    }
                    .setNegativeButton("cancel") { _, _ -> }
                    .show()
            }
        }
    }

    fun init(newContext: Context) {
        context = newContext
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun finish() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun isNetworkAvailable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        }
        return false
    }
}