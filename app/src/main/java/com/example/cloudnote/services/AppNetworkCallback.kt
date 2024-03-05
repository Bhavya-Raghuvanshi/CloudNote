package com.example.cloudnote.services

import android.content.Context
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.widget.Toast

class AppNetworkCallback(private val context: Context) : NetworkCallback()
{
    override fun onAvailable(network: Network)
    {
        Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
    }

    override fun onLost(network: Network)
    {
        Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
    }
}