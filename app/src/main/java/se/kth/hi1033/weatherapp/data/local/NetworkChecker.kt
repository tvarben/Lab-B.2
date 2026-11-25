package se.kth.hi1033.weatherapp.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

interface NetworkChecker {
    fun isOnline(): Boolean
}

class AndroidNetworkChecker(private val context: Context) : NetworkChecker {
    override fun isOnline(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
